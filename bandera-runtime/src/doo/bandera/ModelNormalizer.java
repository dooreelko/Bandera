package doo.bandera;

//TODO: this goes to Bandera-runtime

/***
 * Provide always same number and sequence 
 * of model values of the same model for the ModelBinder
 * @author doo
 *
 */
public interface ModelNormalizer {
	public enum ViewState {
		NotSet,
		Normal,
		ReadOnly,
		Invisible,
		Gone
	}
	
	Object[] getModelValues();
	ViewState[] getViewStates();
	void setModelValue(Object newValue, int position);
}