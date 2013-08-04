package doo.bandera;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import doo.bandera.annotations.BindModel;
import doo.bandera.annotations.BindState;

@SupportedAnnotationTypes({ 
	"doo.bandera.annotations.BindModel", 
	"doo.bandera.annotations.BindState" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ModelBindingProcessor extends AbstractProcessor {
	public class BindingInfo {
		public ExecutableElement getter;
		public ExecutableElement setter;
		public ExecutableElement viewStater;

		@Override
		public String toString() {
			return String.format("getter: %s, setter: %s, viewStater: %s", getter != null ? getter.getSimpleName() : "<none>",
					setter != null ? setter.getSimpleName() : "<none>",
					viewStater != null ? viewStater.getSimpleName() : "<none>");
		}
	}
	
	public class ModelResIds {
		public TypeElement modelClass;
		public List<Integer> resIds;

		public ModelResIds(TypeElement modelClass, List<Integer> resIds) {
			this.modelClass = modelClass;
			this.resIds = resIds;
		}
	}

	private Filer filer;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);

		filer = env.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		
		try {
			Map<Element, Map<Integer, BindingInfo>> classBindings = new Hashtable<Element, Map<Integer, BindingInfo>>();
	
			buildGettersAndSetters(env, classBindings);
	
			buildViewStaters(env, classBindings);
			
			generateJava(classBindings);
		} catch (Exception e) {
			processingEnv.getMessager().printMessage(ERROR, e.toString());
		}
		return true;
	}

	protected void buildGettersAndSetters(RoundEnvironment env, Map<Element, Map<Integer, BindingInfo>> classBindings) {
		for (Element elem : env.getElementsAnnotatedWith(BindModel.class)) {
			Element modelClass = elem.getEnclosingElement();

			ensureModelClassInBindings(classBindings, modelClass);

			int[] resIds = elem.getAnnotation(BindModel.class).value();

			for (int resId : resIds) {
				ensureBindingInfo(classBindings, modelClass, resId);
				
				if (elem.getSimpleName().toString().startsWith("get")) { // TODO: "is, has" if needed
					classBindings.get(modelClass).get(resId).getter = (ExecutableElement) elem;
				} else if (elem.getSimpleName().toString().startsWith("set")) {
					classBindings.get(modelClass).get(resId).setter = (ExecutableElement) elem;
				}
			}
		}
	}

	protected void buildViewStaters(RoundEnvironment env, Map<Element, Map<Integer, BindingInfo>> classBindings) {
		for (Element elem : env.getElementsAnnotatedWith(BindState.class)) {
			Element modelClass = elem.getEnclosingElement();

			ensureModelClassInBindings(classBindings, modelClass);
			int[] resIds = elem.getAnnotation(BindState.class).value();
			for (int resId : resIds) {
				ensureBindingInfo(classBindings, modelClass, resId);
				
				classBindings.get(modelClass).get(resId).viewStater = (ExecutableElement)elem;
			}
		}
	}

	protected void ensureBindingInfo(Map<Element, Map<Integer, BindingInfo>> classBindings, Element modelClass,
			int resId) {
		if (!classBindings.get(modelClass).containsKey(resId)) {
			classBindings.get(modelClass).put(resId, new BindingInfo());
		}
	}

	protected void ensureModelClassInBindings(Map<Element, Map<Integer, BindingInfo>> classBindings, Element modelClass) {
		if (!classBindings.containsKey(modelClass)) {
			classBindings.put(modelClass, new Hashtable<Integer, ModelBindingProcessor.BindingInfo>());
		}
	}

	protected void generateJava(Map<Element, Map<Integer, BindingInfo>> classBindings) {
		List<ModelResIds> binderConfigs = new ArrayList<ModelResIds>();

		for (Entry<Element, Map<Integer, BindingInfo>> pair : classBindings.entrySet()) {
			TypeElement modelClass = (TypeElement) pair.getKey();
			Map<Integer, BindingInfo> thatClassBindings = pair.getValue();

			List<Integer> resIds = new ArrayList<Integer>(thatClassBindings.keySet());
			Collections.sort(resIds);
			
			String normalizerClassName = modelClass.getQualifiedName() + "Normalizer";
			writeCodeForClass(modelClass, normalizerClassName, BrutalWriter.renderNormalizer(modelClass, resIds, thatClassBindings));
		
			binderConfigs.add(new ModelResIds(modelClass, resIds));
		}

		if (!classBindings.isEmpty()) {
			writeCodeForClass(null, "doo.bandera.Models", BrutalWriter.renderModels(binderConfigs));
		}
	}

	protected void writeCodeForClass(TypeElement modelClass, String className, String classText) {

		try {
			JavaFileObject jfo;
			
			if (modelClass != null) {
				jfo = filer.createSourceFile(className, modelClass);
			} else {
				jfo = filer.createSourceFile(className);
			}
			
			Writer writer = jfo.openWriter();
			writer.write(classText);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			error(modelClass, "Unable to write injector for type %s: %s", modelClass, e.getMessage());
		}
	}

	protected void note(Element element, String message, Object... args) {
		processingEnv.getMessager().printMessage(NOTE, String.format(message, args), element);
	}

	protected void error(Element element, String message, Object... args) {
		processingEnv.getMessager().printMessage(ERROR, String.format(message, args), element);
	}

}
