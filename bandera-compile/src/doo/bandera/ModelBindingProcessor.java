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

import doo.bandera.annotations.Bind;

@SupportedAnnotationTypes({ "doo.bandera.annotations.Bind" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ModelBindingProcessor extends AbstractProcessor {
	public class BindingInfo {
		public ExecutableElement getter;
		public ExecutableElement setter;

		@Override
		public String toString() {
			return String.format("getter: %s, setter: %s", getter != null ? getter.getSimpleName() : "<none>",
					setter != null ? setter.getSimpleName() : "<none>");
		}
	}
	
	public class ModelResIds {
		public String modelName;
		public List<Integer> resIds;

		public ModelResIds(String modelName, List<Integer> resIds) {
			this.modelName = modelName;
			this.resIds = resIds;
		}
	}

	private Filer filer;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);

		/*
		 * elementUtils = env.getElementUtils(); typeUtils = env.getTypeUtils();
		 */
		filer = env.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		Map<Element, Map<Integer, BindingInfo>> classBindings = new Hashtable<Element, Map<Integer, BindingInfo>>();

		for (Element elem : env.getElementsAnnotatedWith(Bind.class)) {
			note(elem, "Starting processing element");

			Element modelClass = elem.getEnclosingElement();
			note(elem, "Detected class %s", modelClass.getSimpleName());

			if (!classBindings.containsKey(modelClass)) {
				classBindings.put(modelClass, new Hashtable<Integer, ModelBindingProcessor.BindingInfo>());
			}

			int[] resIds = elem.getAnnotation(Bind.class).value();

			for (int resId : resIds) {
				if (!classBindings.containsKey(modelClass)){
					classBindings.put(modelClass, new Hashtable<Integer, ModelBindingProcessor.BindingInfo>());
				}					
				
				if (!classBindings.get(modelClass).containsKey(resId)) {
					classBindings.get(modelClass).put(resId, new BindingInfo());
				}
				
				if (elem.getSimpleName().charAt(0) == 'g') {
					classBindings.get(modelClass).get(resId).getter = (ExecutableElement) elem;
				} else if (elem.getSimpleName().charAt(0) == 's') {
					classBindings.get(modelClass).get(resId).setter = (ExecutableElement) elem;
				}
			}
		}

		generateJava(classBindings);

		return true;
	}

	protected void generateJava(Map<Element, Map<Integer, BindingInfo>> classBindings) {
		List<ModelResIds> binderConfigs = new ArrayList<ModelResIds>();
		
		for (Entry<Element, Map<Integer, BindingInfo>> pair : classBindings.entrySet()) {
			TypeElement modelClass = (TypeElement) pair.getKey();
			Map<Integer, BindingInfo> thatClassBindings = pair.getValue();

//			String classText = "/*\n";
//
//			classText += String.format("Collected following for %s\n", modelClass.getSimpleName());
//			for (Entry<Integer, BindingInfo> infos : thatClassBindings.entrySet()) {
//				classText += String.format("ResId: %d, bindings: %s\n", infos.getKey(), infos.getValue().toString());
//			}
//
//			classText += "*/";

			List<Integer> resIds = new ArrayList<Integer>(thatClassBindings.keySet());
			Collections.sort(resIds);
			
			String normalizerClassName = modelClass.getQualifiedName() + "Normalizer";
			writeCodeForClass(modelClass, normalizerClassName, BrutalWriter.renderNormalizer(modelClass, resIds, thatClassBindings));
		
			binderConfigs.add(new ModelResIds(modelClass.getQualifiedName().toString(), resIds));
		}

		writeCodeForClass(null, "doo.bandera.Models", BrutalWriter.renderModels(binderConfigs));
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
