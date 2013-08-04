package doo.bandera;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;

import doo.bandera.ModelBindingProcessor.BindingInfo;
import doo.bandera.ModelBindingProcessor.ModelResIds;

public class BrutalWriter {
	public static String renderNormalizer(TypeElement modelClass, List<Integer> resIds,
			Map<Integer, BindingInfo> thatClassBindings) {

		String simpleName = modelClass.getSimpleName().toString();
		String namespace = getNamespace(modelClass);

		StringWriter sw = new StringWriter();

		sw.append(String.format("/* This file is (and will be) generated by bandera. */\n"));
		sw.append(String.format("package %s;\n\n", namespace));

		sw.append(String.format("import %s;\n", modelClass.getQualifiedName()));

		sw.append(String.format("import doo.bandera.ModelNormalizer;\n"));
		sw.append(String.format("import doo.bandera.Parsers;\n"));

		sw.append(String.format("public class %sNormalizer implements ModelNormalizer {\n", simpleName));

		sw.append(String.format("	private final %s model;\n", simpleName));

		sw.append(String.format("	public %sNormalizer(final %s model) {\n", simpleName, simpleName));
		sw.append(String.format("		this.model = model;\n"));
		sw.append(String.format("		}\n"));

		sw.append(String.format("		public %s getModel() {\n", simpleName));
		sw.append(String.format("			return model;\n"));
		sw.append(String.format("		}\n"));

		
		
		sw.append(String.format("		@Override\n"));
		sw.append(String.format("		public Object[] getModelValues() {\n"));
		sw.append(String.format("			return new Object[] {\n"));

		for (int resId : resIds) {
			BindingInfo info = thatClassBindings.get(resId);
			if (info.getter != null) {
				sw.append(String.format("				model.%s(),\n", thatClassBindings.get(resId).getter.getSimpleName()));
			} else {
				sw.append(String.format("				null,\n"));
			}
		}

		sw.append(String.format("			};\n"));
		sw.append(String.format("		}\n"));

		
		
		
		
		sw.append(String.format("		@Override\n"));
		sw.append(String.format("		public ViewState[] getViewStates() {\n"));
		sw.append(String.format("			return new ViewState[] {\n"));

		for (int resId : resIds) {
			BindingInfo info = thatClassBindings.get(resId);
			if (info.viewStater != null) {
				sw.append(String.format("				model.%s(),\n", info.viewStater.getSimpleName()));
			} else {
				sw.append(String.format("				ViewState.NotSet,\n"));
			}
		}

		sw.append(String.format("			};\n"));
		sw.append(String.format("		}\n"));
		

		
		
		sw.append(String.format("		@Override\n"));
		sw.append(String.format("		public void setModelValue(final Object newValue, final int position) {\n"));
		sw.append(String.format("			switch(position) {\n"));

		int pos = 0;
		for (int resId : resIds) {
			sw.append(String.format("			case %d:\n", pos++));
			BindingInfo info = thatClassBindings.get(resId);

			if (info.setter != null) {
				sw.append(String.format("				model.%s(Parsers.SafeParse(newValue, model.%s()));\n",
						info.setter.getSimpleName(), info.getter.getSimpleName()));
			}
			sw.append(String.format("				break;\n"));
		}

		sw.append(String.format("		}\n"));
		sw.append(String.format("	}\n"));
		sw.append(String.format("}\n"));

		return sw.toString();
	}

	public static String getNamespace(TypeElement modelClass) {
		return modelClass.getQualifiedName()
				.subSequence(0, modelClass.getQualifiedName().length() - modelClass.getSimpleName().toString().length() - 1).toString();
	}

	public static String renderModels(List<ModelResIds> binderConfigs) {
		try {

			StringWriter sw = new StringWriter();

			sw.append(String.format("package doo.bandera;\n"));

			sw.append(String.format("public class Models {\n"));

			for (ModelResIds mi : binderConfigs) {
				sw.append(String.format("	public static doo.bandera.ModelBinder Bind(android.app.Activity where, %s model) {\n", mi.modelClass.getQualifiedName()));
				 sw.append(String.format("		return new ModelBinder(where, new %sNormalizer(model),\n", mi.modelClass.getQualifiedName()));
				 sw.append(String.format("				new android.view.View[] {\n"));
				
				 for (int resId : mi.resIds) {
					 sw.append(String.format("						where.findViewById(%d),\n", resId));
				 }
				
				 sw.append(String.format("					});\n"));
				 sw.append(String.format("	}\n"));
			}

			sw.append(String.format("}\n"));

			return sw.toString();
		} catch (Exception e) {
			return e.toString();
		}
	}
}