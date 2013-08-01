package doo.bandera;

//TODO: this goes to Bandera-runtime

/***
 * Provide always same number and sequence 
 * of model values of the same model for the ModelBinder
 * @author doo
 *
 */
public interface ModelNormalizer {
	Object[] getModelValues();
	void setModelValue(Object newValue, int position);
}