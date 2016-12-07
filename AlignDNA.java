/**
 * AlignDNA -- App class for AlignDNA solitaire game
 * 
 * rdb
 * 03/10/09
 */
import javax.swing.*;
import java.awt.*;

public class AlignDNA extends JFrame
{
   //---------------------- instance variables ----------------------
   private GUI _appPanel;      // the app's JPanel
   
   //--------------------------- constructor -----------------------
   public AlignDNA( String title, String[] args )     
   {
      super( title );
 
      this.setBackground( Color.LIGHT_GRAY );
      this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      
      // optional command line arguments are reference file and reads file
      int argIndex = 0;
      
      if ( args.length > 0 && args[ 0 ].equals( "-b" ) )
      {
         GUI.batch = true;
         argIndex = 1;
      }
      _appPanel = new GUI( this, Utilities.getArg( args, argIndex, null ),
                                 Utilities.getArg( args, argIndex + 1, null ));
      if ( GUI.batch )
         return;
      
      this.add( _appPanel );
            
      this.setSize( 1000, 600 );

      this.setVisible( true );
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
