package doo.bandera;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("doo.bundle.annotations.Bind")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ModelBindingProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> arg0,
			RoundEnvironment env) {
		// TODO Auto-generated method stub
		return false;
	}

}
