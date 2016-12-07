/**
 * Aligner.java -- Aligns read sequences against a reference genome
 * 
 * Modified 
 * kj @ Spring 2012
 */
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class Aligner
{
   //------------------- class variables ------------------------------
   //------------------ instance variables ----------------------------
   
   private FastaFile    _reference = null;
   private FastaFile    _reads = null;
   private String       _refDNA;
   private boolean      _alignAlways = true;
   private int          _minPercentMatch = 80;
   private PrintWriter  _log = null;
   private int _matchCounter = 0;
   //------------------- constructor ----------------------------------
   public Aligner() 
   {
      File logFile = new File( "alignmentLog.txt" );
      try
      {
      if ( !logFile.exists() )
         logFile.createNewFile();
      FileWriter fw = new FileWriter( logFile, false );  // append
      _log = new PrintWriter( fw, true );
      }
      catch ( IOException ioe )
      {
         System.err.println( "***Error: unable to open log file, no logging!" );
      }
   }
   //----------------------- setReference( FastaFile ) --------------------
   /**
    * set the reference genome
    */
   public void setReference( FastaFile ref )
   {
      _reference = ref;
      _refDNA = _reference.getComposite();
   }
   //----------------------- setReads( FastaFile ) --------------------
   /**
    * set the reads  
    */
   public void setReads( FastaFile reads )
   {
      _reads = reads;
   }
   //---------------------- minimumPercentMatch( int  ) ----------------
   /**
    * Set the minimum score for doing alignment ( 50 - 98 )
    */
   public void minimumPercentMatch( int minScore )
   {
      _minPercentMatch = minScore;
   }
   //---------------------- align() -----------------------------------
   /**
    * align all the reads in _reads with the reference, then update. This is
    * the "normal" mode of execution, but it is hard to debug.
    */
   public void align()
   {
      if ( _reads == null )
         return;
      int unaligned = 0;
      for ( int i = 0; i < _reads.size(); i++ )
      {
         if ( !align( i ) )
            unaligned++;
      }
      GUI.setUnalignedCount( unaligned );
   }
   //---------------------- align( int ) -----------------------------------
   /**
    * align the single read specified by the parameter. This is useful for
    * debugging; the framework will display that read in a pop up after return
    * from here.
    */
   public boolean align( int read )
   {
      if ( _reads == null )
         return false;
      boolean success = true;
      if ( read >= 0 && read < _reads.size() )
      {
         DNASequence seq = _reads.get( read );
         //System.out.println( "align: " + seq.getId() );
         int loc = findAlign( seq );
         if ( loc >= 0 )
         {
            addToDisplay( seq, loc );
         }
         else
         {
            success = false;
         }
      }
      else
      {
         success = false;
         System.err.println( "Invalid read index: " + read );
      }
      return success;
   }
   //---------------------- align( int, int ) -----------------------------------
   /**
    * align the set of reads from first to last inclusive. This is also useful
    * for debugging; if you have a problem with reads in the middle, you can
    * do everything up to the problem point in one call, then single step
    * after that.
    */
   public int align( int first, int last )
   {
      if ( _reads == null )
         return 0;
      int unaligned = 0; 
      for ( int i = first; i <= last; i++ )
      {
         if ( ! align( i ) )
            unaligned++;
      }
      return unaligned;
      //System.out.println( "Leaving align( " + first + ", " + last + " )" );
   }
   //------------------------ findAlign( DNASequence ) -----------------------
   /**
    * Implement a heuristic algorithm to find what hopefully is the closest 
    * matching location for this sequence in the reference.
    * If not successful, report failure to log. 
    */
   private int findAlign( DNASequence seq )
   {
     
     int pos = -1;
     String str = seq.getDNA();
     _refDNA = _reference.getComposite();
     Hit best = new Hit(-1, -1, 0);
     
     int notAlignedCounter = 0;
     
     for(int i = 0; i < str.length() - 8; i += 8)
     {
       Pattern patt = Pattern.compile(str.substring(i, i+8));
       Matcher match = patt.matcher(_refDNA);
       
       while(match.find())
       {
         int score = extendMatch(seq, match.start(), i);
         Hit hit = new Hit(match.start(), i, score);
         
         if(best.score < hit.score)
         {
           best = hit;
         }
       }
     }
     
     if(best.score <= 0)
     {
       notAlignedCounter++;
       _log.write("\n" + seq.getHeader() + ": Is Not Aligned " + best + 
                  "\n score is : " + best.score);
       return -1;
     } 
     else if(best.score < _minPercentMatch)
     {
       notAlignedCounter++;
       _log.write("\n" + seq.getHeader() + ": Is Not Aligned " + best + 
                  "\n score is: " + best.score);
       return -1;
     } 
     
     else
     {
       pos = best.posInRef - best.posInSeq;
       _log.write("\n new Aliign: " + pos + " \nscore is : " + best.score);
       
       GUI.setUnalignedCount( notAlignedCounter );
       _log.flush();
     }
     return pos;
   }
   
   
   //----------------------- extend( DNASequence, int, int) ----------------------
   public int extendMatch( DNASequence s, int refPos, int seqPos )
   {
     _matchCounter = 8;  
     int score = 0;
     int bScore = 0;
     
     String sequence = s.getDNA();
     int sPosition = seqPos;
     
     for(int rPosition = refPos; rPosition < _refDNA.length(); rPosition++, score++)
     {
       if(sequence.length() <= sPosition)
       {
         break;
       } 
           
       else if(_refDNA.charAt(rPosition) == sequence.charAt(sPosition))
       {
         _matchCounter++;
       }
       else if(score >= 24 && sPosition / _matchCounter > 2)
       {
         _log.write("\n Early Termination is : " + s.getHeader() + " _matchCounter is: " + _matchCounter 
                   + " \nscore: " + bScore); 
         _log.flush();
         return 0;
       }
       sPosition++;
     }
     
     for(int i = refPos; i < 0; i--, bScore++)
     {
       if(sPosition < 0)
       {      
         break;
       } 
       else if(_refDNA.charAt(i) == sequence.charAt(sPosition))
       {
         _matchCounter++;
       } 
       
       else if(bScore >= 24 && sPosition / _matchCounter > 2)
       {
         _log.write("\n Early Termination is : " + s.getHeader() + " matchCount is : " 
                      + _matchCounter + "\nscore is : " + bScore); 
         _log.flush();
         return 0;
       }
       sPosition--;
     }
     return ((_matchCounter*100)/ s.length());


                 
   }
   //----------------------- addToDisplay() ----------------------------------
   /**
    * prepare the DNASequence display data and add it to the display
    */
   private void addToDisplay( DNASequence seq,  int pos )
   {
      seq.setReferencePosition( pos );
      DisplayPanel.addRead( seq );
   }
   //---------------------------- log( String ) -------------------------------
   /**
    * Log key information to output log
    */
   private void log( String out )
   {
      // for now, just write to standard out
      if ( _log != null )
         _log.println( out );
   }
   //+++++++++++++++++++++++++ private inner class +++++++++++++++++++++++++++
   //------------------ class Hit --------------------------------------------
   private class Hit
   {
      public int posInRef;
      public int posInSeq;
      public int score;
      
      public Hit( int pr, int ps, int s )
      {
         posInRef = pr;
         posInSeq = ps;
         score = s;
      }
      public String toString()
      {
         return "<" + posInRef + "," + posInSeq + "," + score + ">";
      }
   }
}
