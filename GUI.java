/** 
 * GUI.java:
 * A JPanel to control the application's user interface and its 
 * connection to the main application code.
 * 
 * This GUI supports the AlignDNA program
 *
 * @author rdb
 * 
 */

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GUI extends JPanel 
{
   //------------------ class variables ---------------------------
   static private GUI    theGUI;   
   public static boolean batch = false;
   
   //---------------- instance variables ---------------------------
   private Container     _parent;     // not really used
   private DisplayPanel  _display;
   private FastaFile     _reference;   // the target genomem to align against 
   private FastaFile     _reads;      // the reads to be aligned
   
   private JLabel        _referencePosition; // loc of mouse in reference
   private int           _refPos;
   private JLabel        _sequencePicked; // loc of mouse in reference
   private String        _sequencePickedId = "";
   private JLabel        _unaligned;
   private int           _unalignedCount = 0;

   private Aligner       _aligner;
   private int           _nextToAlign = 0;
      
   //------------------- constructor -------------------------------
   /**
    * Container parent of the control panel is passed as an argument
    * along with the application object.
    */
   public GUI( Container parent, String referenceFileName, 
                                 String readsFileName ) 
   {
      super ( new BorderLayout() );
      theGUI   = this;
      _parent  = parent; 
 
      _display = new DisplayPanel( this );
      _aligner = new Aligner();
      
      if ( GUI.batch )
      {
         if ( referenceFileName == null || readsFileName == null )
         {
            System.err.println( "Need to specify both files for batch" );
         }         
         readReference( referenceFileName );
         readReads( readsFileName );
         _aligner.align();
         return;
      }
      
      int displayWidth = _display.getPreferredSize().width; 
      int displayHeight = _display.getPreferredSize().height;
      
      final JScrollPane sPane = new JScrollPane( _display,
               ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
      );     
      this.add( sPane, BorderLayout.CENTER );
      
      //create Buttons in the North
      Component buttonMenu = makeButtonMenu();
      this.add( buttonMenu, BorderLayout.NORTH );
      
      JPanel southPanel = makeSouth();
      this.add( southPanel, BorderLayout.SOUTH );
      
      sPane.setPreferredSize( new Dimension( displayWidth, displayHeight )); 
         
      if ( referenceFileName != null )
         readReference( referenceFileName );
      if ( readsFileName != null )
         readReads( readsFileName );
   }
   //------------------- makeSouth() -----------------------------------
   /**
    * Create three sliders and 2 JLabels in the south region
    */
   private JPanel makeSouth()
   {
      //create display panel size scrollbar in the south
      JPanel southPanel = new JPanel( new GridLayout( 2, 3 ));
      
      southPanel.add( 
         new LabeledSlider( "Nucleotide width", 1, 11, 10 )
         { 
            public void valueChanged( int newWidth )
            {
               _display.setNucleotideWidth( newWidth );
            }
         }
      ); 
      
      int displayWidth = _display.getPreferredSize().width;
      southPanel.add( 
         new LabeledSlider( "Pane width", 500, 100000, displayWidth )
         { 
            public void valueChanged( int newWidth )
            {
               int       h = _display.getPreferredSize().height;
               _display.updateSize( new Dimension( newWidth, h ));
               this.revalidate();
            }
         }
      ); 
      
      southPanel.add( 
         new LabeledSlider( "Min score ( 80 )", 50, 95, 80 )
         { 
            public void valueChanged( int newMinScore )
            {
               _aligner.minimumPercentMatch( newMinScore );
               this.setText( "Min score ( " + newMinScore + " )" );
            }
         }
      ); 
      
      // create a label for reference  position of mouse
      _referencePosition = new JLabel( "  Reference position: " + _refPos ); 
      _referencePosition.setBorder( new LineBorder( Color.BLACK ) );
      southPanel.add( _referencePosition );
      
      // create a label for last picked sequence
      _sequencePicked = new JLabel( "  Picked sequence: " + _sequencePickedId );
      _sequencePicked.setBorder( new LineBorder( Color.BLACK ) );
      southPanel.add( _sequencePicked );

      // create a label for unaligned count
      _unaligned = new JLabel( " Unaligned count: " + _unalignedCount );
      _unaligned.setBorder( new LineBorder( Color.BLACK ) );
      southPanel.add( _unaligned );
      return southPanel;
   }

   //----------------- setReferencePosition --------------------------
   public void setReferencePosition( int pos )
   {
      _refPos = pos;
      _referencePosition.setText( "  Reference position: " + _refPos + "  " );
   }
   //----------------- setSequencePicked( String ) --------------------------
   public void setSequencePicked( String id )
   {
      _sequencePickedId = id;
      _sequencePicked.setText( "  Picked sequence: " + id );
   }
   //----------------- setUnalignedCount( int ) --------------------------
   public static void setUnalignedCount( int count )
   {
      theGUI._unalignedCount = count;
      if ( GUI.batch )
         return;
      theGUI._unaligned.setText( "  Unaligned sequences: " + count );
   }
   //------------------- makeButtonMenu --------------------------------
   private Component makeButtonMenu()
   {
      // JPanel defaults to FlowLayout
      String[] labels = { "Reference File", "Reads File", "Align all", 
                          "Align range", "Align first", "Align next" };
      
      JPanel bMenu = new JPanel( new GridLayout( 1, 0 )); 
      JButton button;
      for ( int i = 0; i < labels.length; i++ )
      {
         button = new JButton( labels[ i ] );
         //button.setFont( getFont().deriveFont( 11.0f ));
         bMenu.add( button );
         button.addActionListener( new ButtonListener( i ));
      }      
      return bMenu;
   }
   //+++++++++++++++++ ButtonListener inner class ++++++++++++++++++++++++
   /**
    * ButtonListener handles all button events and passes them along
    * to methods of the ListPanel class.
    */
   private class ButtonListener implements ActionListener
   {
      int _buttonId;
      public ButtonListener( int buttonId )
      {
         _buttonId = buttonId;
      }
      public void actionPerformed( ActionEvent ev )
      {
          switch ( _buttonId )
          {
             case 0:
                readReference();
                break;
             case 1:
                readReads();
                break;
             case 2:
                alignAll();
                break;
             case 3:
                alignRange();
                break;
             case 4:
                alignFirst();
                break;
             case 5:
                alignNext();
                break;
          }               
      }
   } 
   //---------------------- readReference() -------------------------------
   private void readReference( )
   {
      String fileName = Utilities.getFileName( "Choose Reference file" );
      if ( fileName != null && fileName.length() != 0 )
         readReference( fileName );
   }
   //-------------------- readReference( String ) ------------------------------
   private void readReference( String fileName )
   { 
      _reference = new FastaFile( fileName );
      _display.setReference( _reference.getComposite() );
      _aligner.setReference( _reference ); 
   }
   //---------------------- readReads() -------------------------------
   private void readReads( )
   {
      String fileName = Utilities.getFileName( "Choose Read file" );
      if ( fileName != null && fileName.length() != 0 )
         readReads( fileName );
   }
   //-------------------- readReads( String ) ------------------------------
   private void readReads( String fileName )
   { 
      //System.out.println( "Reads" );
      _reads = new FastaFile( fileName );
      //System.out.println( "Read count: " + _reads.size());
      _aligner.setReads( _reads );
      _display.clearReads();
   }
   
   //-------------------- alignAll(  ) ------------------------------
   private void alignAll() 
   { 
      _display.clearReads();
      _aligner.align();
      _display.update();
   }
   
   //-------------------- alignRange(  ) ------------------------------
   private void alignRange() 
   { 
      String reply = JOptionPane.showInputDialog( null,
                                "Enter 1 integer or 2 for first and last" );
      if ( reply != null && reply.length() > 0 )
      {
         try
         {
            Scanner in = new Scanner( reply );
            int first = in.nextInt();
            int last = first;
            if ( in.hasNextInt() )
               last  = in.nextInt();
            int count = _aligner.align( first, last );
            if ( count > 0 )
               JOptionPane.showMessageDialog( null, 
                                       count + " reads did not align" );
            _display.update();
            _nextToAlign = last + 1;
         }
         catch ( Exception ex )
         {
            JOptionPane.showMessageDialog( null, "Invalid input: " + reply );
         }
      }
   }

   //-------------------- alignFirst(  ) ------------------------------
   private void alignFirst() 
   { 
      if ( _reads != null && _reads.size() > 0 )
      {
         boolean aligned = _aligner.align( 0 );
         _nextToAlign = 1;
         if ( !aligned )
           JOptionPane.showMessageDialog( null, "Read did not align" );
      }
      else
         JOptionPane.showMessageDialog( null, "No reads!" );
      
      _display.update();
   }
  
   //-------------------- alignNext(  ) ------------------------------
   private void alignNext() 
   { 
      if ( _reads.size() <= _nextToAlign )
          JOptionPane.showMessageDialog( null, "No more reads!" );
      else if ( ! _aligner.align( _nextToAlign++ ) )
          JOptionPane.showMessageDialog( null, "Read did not align" );
     
      _display.update();
   }
  
   //------------------ main ------------------------------------------   
   public static void main( String [ ] args ) 
   {
      String myArgs[] = { "smallRef.txt", "smallReads.txt" };
      if ( args.length == 0 )
         new AlignDNA( "AlignDNA", myArgs );
      else
         new AlignDNA( "AlignDNA", args );
   }
}
