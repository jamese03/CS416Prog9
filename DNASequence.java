/**
 * DNASequence - a class to represent a DNA sequence from a FASTA format
 * file. It includes header and sequence information.
 * 
 * @author rdb
 * Created 11/08/08
 */

import java.awt.Color;

public class DNASequence implements Comparable<String>
{
   //-------------------  class variable s --------------------------------
   private final static int           seqLineLen = 50;
   private final static String        validNucChars  = "AaTtGgCcXxNn-.";
   private final static String        complementChars = "TtAaCcGgXxNn-.";
   
   //----------------------- Instance variables ---------------------------
   private String        dnaString;
   private String        seqId;
   private String        seqHeader;
   
   // refPos gets mapped to X, overlapCount gets mapped to Y
   private int           refPos = -1;      // alignment of start to reference
   private int           overlapCount = 0; // num sequences that overlap with
                                           //  this one already displayed.
   private int           seqLen = 0;
    
   //++++++++++++++++++++++++++++ constructors ++++++++++++++++++++++++++++
   //------------------------- DNASequence( String ) -------------------------
   /**
    * Constructor takes the sequence header and the DNA sequence as Strings
    */
   public DNASequence( String header, String dna )
   {  
      String  headerPlus = header + " ";     // guarantee that header ends in blank
      int endId = headerPlus.indexOf( " " );
      seqId = headerPlus.substring( 1, endId );
      seqHeader = header;
      dnaString = dna;
      seqLen = dna.length();
      checkDNA();
      checkId();
   }
   //++++++++++++++++++++++++++++ public methods ++++++++++++++++++++++++++
   //----------------------------- getDNA -------------------------------
    /**
     * return the full dna sequence data as a String
     */
   public String getDNA()
   {
      return this.dnaString;
   }
   //----------------------------- getHeader() -------------------------------
    /**
     * return the full dna sequence data as a String
     */
   public String getHeader()
   {
      return this.seqHeader;
   }
   //----------------------------- getId -------------------------------
    /**
     * return the identifying "name" of the sequence
     */
   public String getId()
   {
      return this.seqId;
   }
   
   //--------------------------- length() --------------------
   /**
    * return the number of nucleotides in this sequence
    */
   final public int length()
   {
      return seqLen;
   }
   //-------------------- setReferencePosition( int p ) -------------------
   /**
    * identify the start of this read in the alignment to the reference
    */
   public void setReferencePosition( int p )
   {
      refPos = p;
   }
   //-------------------- getReferencePosition() -------------------
   /**
    * return the position of this sequence in reference
    */
   public int getReferencePosition()
   {
      return refPos;
   }
   //-------------------- setOverLap( int o ) -------------------
   /**
    * set to the number of reads that overlap this one and are
    * already added to the display; this will become the y display value
    */
   public void setOverLap( int o )
   {
      overlapCount = o;
   }
   //-------------------- getOverlap() -------------------
   /**
    * return the overlap count for this sequence.
    */
   public int getOverlap()
   {
      return overlapCount;
   }
   //----------------------- compareTo( DNASequence ) -------------------
   /**
    * comparison is based on sequence nucleotides
    */
   public int compareTo( DNASequence seq )
   {
      return seqLen - seq.seqLen;
   }
   //----------------------- compareTo( String ) -------------------
   /**
    * comparison is based on sequence nucleotides
    */
   public int compareTo( String seqName )
   {
      return seqId.compareTo( seqName );
   }
   //-------------------------------- toString() -----------------------------
   /**
    * Returns a single string that reproduces an approximation to the input 
    * format for the sequence. In other words, the returned string must start 
    * with a header line that looks like the original header in the input file 
    * and includes the line feed character (\n). 
    * 
    * The header information is followed (in the same String object being 
    * returned) by the sequence data that is broken up into sections that will 
    * print nicely. In other words, you should copy the sequence data to the 
    * output string in groups of 50 characters followed by a line feed. The line 
    * length should be an instance variable so that it can be easily changed.
    */
   public String toString()
   {
      StringBuffer result = new StringBuffer( seqHeader );
      result.append( "\n" );
      int i = 0;
      while ( i < dnaString.length() - seqLineLen )
      {
         result.append( dnaString.substring( i, i + seqLineLen ) );
         result.append( "\n" );
         i += seqLineLen;
      }
      result.append( dnaString.substring( i ) );
      result.append( "\n" );
      return new String( result );
   }
 
    /*************************************************************/
   
   
   //++++++++++++++++++++++++++  private methods ++++++++++++++++++++++++++++
   //------------------- checkDNA() ----------------------------------
   /**
    * check each character to make sure it is a valid character.
    * Throw InvalidDNASequence exception.
    */
   private void checkDNA()
   {
      for ( int i = 0; i < dnaString.length(); i++ )
      {
         char nuc = dnaString.charAt( i );
         if ( validNucChars.indexOf( nuc ) < 0 )
            //System.err.println( "DNASequence ERROR: Bad character: " + nuc );
            throw new DNASequenceException( "Bad character: " + nuc );
      }
   }
   //------------------- checkId() ----------------------------------
   /**
    * check the sequence ID for validity
    */
   private void checkId()
   { 
      if ( seqId.length() < 1 )
         //System.err.println( "DNASequence ERROR: " );
         throw new DNASequenceException( "Empty sequence id." );
   }

   //++++++++++++++++++++++++ public inner class +++++++++++++++++++++++++++
   //------------------------ DNASequenceException --------------------------
   /**
    * DNASequenceException --  thrown when an error is detected in the input
    *          that defines a DNASequence object.
    *  There are 2 conditions that will throw this exception
    *     1. A zero length sequence identifier
    *     2. An invalid nucleotide character. Valid chars are ACTGactgXxNn.-
    */
   public class DNASequenceException extends RuntimeException
   {
      public DNASequenceException( String msg )
      {
         super( "DNASequence ERROR:" + msg );
      }
   }
}
