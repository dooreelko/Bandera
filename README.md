Bandera
=======

Automation for android model-view binding

This little library provides compile-time annotations for model-UI bidirectional binding so you don't need 
to do that boring findViewById-setChangeListener stuff again.

Currently only a subset is implemented: TextView, EditText, DateTimePicker, ProgressBar, ImageView, ImageButton. 
But this is an open source project. Wink-wink, nudge-nudge.

HOW2
======

- Specify bandera-compile.jar as your (yet another) annotation processor.

- Just declare your model class like <pseudocode from here>:

        public class MainActivityModel // always use creative names! {
            private int catCount;
            
            public MainActivityModel(CatRecordFromDb cats) {
              // init your viewmodel here 
              // ...
            }
        
          	@BindModel({ R.id.practice_image, R.id.buttonStart }) // RO binding to imageUri for ImgView and ImgButton
            public String getFancyImage() {
              return getModel().imageUrl;
            }
            
          	@BindModel(R.id.editNumberOfCats) // getter for EditText value initialisation
            public int getCatsCount() {       // method name can be anything, getters just need the "get" in front
              return catCount;                // conversion from int to String happens magically at google offices* (even offline!)
            }
        
            @BindModel(R.id.editNumberOfCats)         // setter to update the model from the control
            public void setCatNumber(int catCount) {  // setter name doesn't need to match getter's, just be "set<Whatever>"
              this.catCount = catCount;               // the value is already converted into target type
              recalculateTotal();                     // do the magic after cat count changed
            }
        }    // * Measurement of correctness of this description is pending indifinitely
    
    

- Then in your MainActivity you do the woodoo in form of

    	  binder = doo.bandera.Models.Bind(this, new MainActivityModel(catsFromDb)); // doo.bandera.Models.Bind for your activity/model pair is generated during compilation
    	  
- If you've updated the model from outside, just call
        
        binder.updateDirtyValues(); // scans what changed in the model and updates widgets
    	
- Specify bandera-runtime.jar as exported dependency for your apk.
- Ready to roll.

And then the magic begins. Widgets are automatically populated from the model, model is updated back after user interactions. 

That's all. 
