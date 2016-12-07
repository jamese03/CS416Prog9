/** 
 * LabeledSlider.java:
 * 
 * A utility class that combines a label and slider and simplifies
 * the code needed to modify a value. 
 * 
 */

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class LabeledSlider extends JPanel 
{
   //-------------------- instance variables ----------------------------
   JSlider _slider;   // references to the 2 components
   JLabel  _label; 
   
   //------------------------ constructors ------------------------------
   /**
    * int argument constructor defines a "value" slider
    *       (as opposed to a % slider).
    */
   public LabeledSlider ( String name, int min, int max, int val, int direction ) 
   {
      super();
      // Create the label and slider and initialize their parameters.
      _label = new JLabel( name );
      this.add( _label );
      _slider = new JSlider( direction, min, max, val );
      _slider.setBorder( new LineBorder( Color.BLACK, 2 ));

      _slider.addChangeListener( new ParameterListener());
      this.add( _slider );
   }
 
   /**
    * Set new text in the label
    */
   public void setText( String text )
   {
      _label.setText( text );
   }
   /**
    * Constructor for default Horizontal layout
    */
   public LabeledSlider ( String name, int min, int max, int val ) 
   {
      this( name, min, max, val, JSlider.HORIZONTAL );
   }

   //--- If extending this class, need to override the valueChanged method --
   protected void valueChanged( int newValue ) {}      // override for value
   
   //---------------- addChangeListener( ChangeListener ) -------------------
   /** 
    * OR, the app can call this method to specify the change listener.
    */
   public void addChangeListener( ChangeListener listener )
   {
      _slider.addChangeListener( listener );
   }
  
   //+++++++++++++++++++++++++++ ParameterListener class +++++++++++++++++++++
   /**
    * Inner class to serve as slider listener.
    */
   private class ParameterListener implements ChangeListener 
   {
      // no constructor needed -- parent works fine
      //-------------- stateChanged( ChangeEvent ) -----------------
      public void stateChanged( ChangeEvent e ) 
      {
            valueChanged( ((JSlider)e.getSource()).getValue() );
      }
   }
}