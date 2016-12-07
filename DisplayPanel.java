/** 
 * DisplayPanel.java: This class supports the display of DNA alignment data.
 *        The subject DNA is a single instance of DNASequence representing
 *        an assembled consensus genome component -- usually a reference.
 *   
 *        DNA sequence "reads" are aligned to the consensus sequence at the location
 *        where they fit best -- if they fit well enough. These are displayed
 *        underneath the consensus, hopefully in a non-overlapping manner.
 * 
 *        There are scrollbars in horizontal and vertical directions.
 * 
 */

import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;

public class DisplayPanel extends JPanel 
{  
   //------------------- class variables --------------------------
   static private  DisplayPanel theDisplay;
   
   //------------------- instance variables ------------------------
   private String    _reference;
   private List<String,DNASequence> _readList;
   
   // variables for organizing reads
   
   // _tracks represent a horizontal strip of display area at a fixed height
   private Vector<List<String,DNASequence>> _tracks; 
   
   //------------- magic constants
   private int       _referenceX = 10; // start of consensus display
   private int       _referenceY = 20;
   private int       _nucHeight = 20;
   
   private int       _readStartY = 70;

   //     
   private int       _nucWidth      = 10;  // space for the nucleotide
   private int       _fontSize      = 16;  // font depends on _nucWidth
   private int[]     _fontSizeArray = { 8, 10, 12, 14, 15, 16, 18 };
          // By experimentation:
          //    (11,18) (10,16) (9,15) (8,14) (7,12) (6,10) (5,8)
   private int       _maxWidth = 11;
   private int       _minTextWidth = 5;
   private int       _minTextHeight = 10;
   private int       _minBorderWidth = 3;
   private int       _readHeight = 4;
   private int       _readOffset = 6;

   private int       _defaultW   = 20000;
   private int       _defaultH   = 1000; 
   
   // variables for handling mouse position mapping
   private GUI       _gui;

   //--------------------- constructor ----------------------------
   public DisplayPanel ( GUI gui ) 
   {
      super();
      theDisplay = this;
      _gui = gui;
      _readList = new List<String,DNASequence>();
      if ( GUI.batch )
         return;
      _tracks   = new Vector<List<String,DNASequence>>();
      setLayout( null );
      setPreferredSize( new Dimension( _defaultW, _defaultH ));
      setupMouseListeners();

      update();
   }
   //----------------------- setupMouseListeners() -----------------------
   private void setupMouseListeners()
   {
      this.addMouseListener(
      new MouseListener() { 
         public void mousePressed( MouseEvent me ) {};
         public void mouseReleased( MouseEvent me ) {};
         public void mouseEntered( MouseEvent me ) {};
         public void mouseExited( MouseEvent me ) {};
         public void mouseClicked( MouseEvent me )
         {
            Point mouseAt = me.getPoint();
            int nucPosition = ( me.getX() - _referenceX ) / _nucWidth;
            int track = ( me.getY() - _readStartY ) / _readOffset;
            String header = findReadPicked( track, nucPosition );
            //System.out.println( "--- picked: " + seqId );
            _gui.setSequencePicked( header );
         }
      } );
      this.addMouseMotionListener(
      new MouseMotionListener() { 
         public void mouseDragged( MouseEvent me ) {};
         public void mouseMoved( MouseEvent me )
         {
            Point mouseAt = me.getPoint();
            int nucPosition = ( me.getX() - _referenceX ) / _nucWidth;
            _gui.setReferencePosition( nucPosition );
         }
      } );
   }      
   //----------------------- setReference( String ) ---------------------
   /**
    * define a new list to display
    */
   public void setReference( String dna )
   {
      _reference = dna;
      update();
   }
   //----------------------- clearReads( ) ----------------
   /**
    * clear the information about reads
    */
   public void clearReads()
   {
      //System.out.println( "DisplayPanel.clearReads()" );
      if ( _tracks != null )
         _tracks.clear();
      if ( _readList != null )
         _readList.clear();
      updateTracks();
      update();
   }
   //----------------------- addRead( DNASequence ) ----------------
   /**
    * add a read to the list to be displayed, put in order of length
    */
   public static void addRead( DNASequence read )
   {
      theDisplay.addReadP( read ); // invoke private version of read
      theDisplay.assignReadToTrack( read );
      theDisplay.update();
   }
   //----------------------- addReadP( DNASequence ) ----------------
   /**
    * add a read to the list to be displayed, put in order of length
    */
   private void addReadP( DNASequence read )
   {
      int i = 0;
      int rLen = read.length();
      
      //System.out.println( "starting list search" );
      DNASequence seq = _readList.first(); 
      while ( seq != null && seq.length() > rLen )
         seq = _readList.next();
      //System.out.println( "done list search " + seq );
     
      if ( seq == null )
         _readList.addTail( read );
      else
      {
         _readList.previous();  // needs to go before the one we stopped at
         _readList.add( read );
      }
   }
   
   //----------------------  setNucleotideWidth( int ) -------------------
   public void setNucleotideWidth( int newW )
   {
      if ( newW > 0 && newW < _maxWidth )
      {
         _nucWidth = newW;
         if ( _nucWidth >= _minTextWidth )
            _fontSize = _fontSizeArray[ _nucWidth - _minTextWidth ];
         _minTextHeight = _nucWidth * 2;
      }   
      update();
   }
   //------------------------ updateSize( Dimension ) -----------------------------
   /**
    * update the size of the display panel
    */
   public void updateSize( Dimension dim )
   {
      this.setPreferredSize( dim );
      this.setSize( dim );
      Dimension mDim = new Dimension( dim.width, dim.height - _referenceY );
      update();
   }
   //------------------------ update() -----------------------------
   /**
    * update the graphical representation of the list being shown
    */
   public void update()
   {
      if ( GUI.batch )
         return;
      this.revalidate();
      this.repaint();
   }
   //------------------------ updateTracks() -----------------------------
   /**
    * update the graphical representation of the list being shown
    */
   public void updateTracks()
   {
      if ( GUI.batch )
         return;
      _tracks = new Vector<List<String,DNASequence>>();
      DNASequence seq = _readList.first();
      while ( seq != null )
      {
         assignReadToTrack( seq );
         seq = _readList.next();
      }
   }
   //--------------- assignReadToTrack( DNASequence ) --------------------
   /**
    * Check each of the tracks from top to bottom 
    *    if read fits in track without overlap with existing reads
    *       add it to the track
    *    if track is empty
    *       create new list for track and add this read to it.
    *       track lists are ordered by starting position   
    */
   private void assignReadToTrack( DNASequence seq )
   {
      if ( GUI.batch ) 
         return;
      int t = 0;   // start at track 0
      boolean assigned = false;
      while ( t < _tracks.size() && !assigned )
         assigned = fitsInTrack( t++, seq );
      if ( !assigned )
      {
         List<String,DNASequence> track = new List<String,DNASequence>();
         track.add( seq );
         _tracks.add( track );
         //System.out.println( seq.getId() + "-> track " + t );
      }
   }
   //-------------------- fitsInTrack( List, DNASequence ) -------------------
   /**
    * check if the sequence can be displayed in this track, if so, do it
    * return true if it fit, false otherwise
    */
   private boolean fitsInTrack( int t, DNASequence seq )
   {
      List<String,DNASequence> track = _tracks.get( t );
      DNASequence s = track.first();
      while ( s != null && !overlap( s, seq ) )
          s = track.next();
      
      if ( s == null )
      {
         //System.out.println( seq.getId() + "-> track " + t );       
         track.add( seq );
         return true;
      }
      else 
         return false;
   }
   //------------------- overlap( DNASequence, DNASequence )--------------
   /**
    * returns true if the two sequences overlap, false otherwise
    */
   private boolean overlap( DNASequence s1, DNASequence s2 )
   {
      int start1 = s1.getReferencePosition();
      int end1 = start1 + s1.length() - 1;
      int start2 = s2.getReferencePosition();
      int end2 = start2 + s2.length() - 1;
      
      return !( start2 > end1 || start1 > end2 );
   }
   //------------------- overlap( DNASequence, int )--------------
   /**
    * returns true if the sequence includes the position
    */
   private boolean overlap( DNASequence s, int pos )
   {
      int start = s.getReferencePosition();
      int end = start + s.length() - 1;
     
      return start <= pos && pos <= end;
   }
   //----------------------- findReadPicked( int, int ) --------------------
   private String findReadPicked( int t, int pos )
   {
      //System.out.println( "Pick: " + t + " " + pos );
      if ( t < 0 || t >= _tracks.size() )
         return null;
      List<String,DNASequence> track = _tracks.get( t );
      
      DNASequence s = track.first();
      while ( s != null && !overlap( s, pos ) )
          s = track.next();
        
      if ( s == null )
         return null;
      else 
         return s.getHeader();
   }
   //----------------------- paintComponent( Graphics ) ---------------------
   /**
    * paintComponent - calls draw and fill awt methods
    */
   public void paintComponent( java.awt.Graphics brush )
   {
      // System.out.println( "paintComponent called " );
      super.paintComponent( brush );
      
      Graphics2D brush2 = (Graphics2D) brush;
      drawDNA( brush2, _reference, _referenceX, _referenceY, _nucHeight );
      
      drawReads( brush2 );
   }
   //------------ drawReads( Graphics2D  ) ---------------------
   /**
    * Generate display for the reads
    */
   private void drawReads( Graphics2D brush )
   {
      if ( _readList == null || _readList.size() == 0 )
         return;
      int trackY = _readStartY;
      for ( List<String,DNASequence> track: _tracks )
      {
         DNASequence seq = track.first();
         while ( seq != null )
         {
            int p = seq.getReferencePosition();
            if ( p >= 0 )
            {
               int len = seq.getDNA().length();
               //System.out.println( "Pos,len: " + p + " " + len );
               drawDNA( brush, seq.getDNA(), 
                       _referenceX + p * _nucWidth,
                       trackY + ( _readOffset ) * seq.getOverlap(), 
                       _readHeight );
            }
            seq = track.next();
        }
         trackY += _readOffset;
      }
      
   }
   //------------ drawDNA( Graphics2D, String, int, int, int  ) ---------------------
   /**
    * Generate display for a dna sequence
    */
   private void drawDNA( Graphics2D brush, String dna, 
                         int xStart, int yStart, int height )
   {
      if ( dna == null )
         return;
      int dx = _nucWidth;
      int dy = height;
      int x = xStart;
      int y = yStart;
      
      for ( int n = 0; n < dna.length(); n++ )
      {
         switch ( dna.charAt( n ) )
         {
            case 'A': case 'a': brush.setColor( Color.GREEN ); break;
            case 'T': case 't': brush.setColor( Color.RED ); break;
            case 'G': case 'g': brush.setColor( Color.YELLOW ); break;
            case 'C': case 'c': brush.setColor( Color.BLUE ); break;
            case 'N': case 'n': brush.setColor( Color.MAGENTA ); break;
            case 'X': case 'x': brush.setColor( Color.GRAY ); break;
            case '-':           brush.setColor( Color.BLACK ); break;
            case '*':           brush.setColor( Color.WHITE ); break;
            default:            brush.setColor( Color.GRAY ); break;
         }
         brush.fillRect( x, y, dx, dy );
         if ( _nucWidth >= _minBorderWidth )
         {
            brush.setColor( Color.BLACK );
            brush.drawRect( x, y, dx, dy );
         }
         x += dx;
      } 
      if ( _nucWidth >= _minTextWidth && height >= _minTextHeight )
      {
         brush.setFont( new Font( "Monospaced", Font.PLAIN, _fontSize ));
         brush.setColor( Color.BLACK );
         int textBaseline = (int) ( dy * 0.75 );
         brush.drawString( dna, xStart, yStart + textBaseline );
      }
   }   
}
