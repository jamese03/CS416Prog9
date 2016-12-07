/**
 * FastaFile -- This class implements an interface to a file containing
 *       DNA sequences in FASTA format. This implementation reads the entire
 *       file at once and stores the sequences into a Vector of DNASequence
 *       objects. This is a viable implementation only for very small 
 *       data sets -- an application designed to use real genome data
 *       would not be able to fit all the data in memory at one time.
 * 
 *       Key public methods:
 *           FastaFile( String filename ) -- filename must be a valid readable
 *                    Fasta file or else a FileNotFoundException is thrown.
 *           iterator<DNASequence>() -- returns an iterator over the sequences
 *                    in the file.
 *           DNASequence get( int i ) -- returns the i-th sequence in the file
 *                     if it exists; otherwise it returns null
 *           int size() -- returns the number of sequences in the file
 * 
 * @author rdb
 * April 25, 2009
 * 
 */
import java.awt.Point;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class FastaFile implements Iterable
{
   //------------------------- class variables ---------------------------------
   
   //------------------------- instance variables ------------------------------
   private Scanner scanner = null;
   private String  nextHeader   = null;
   private int     targetength = 0;
   private Vector<DNASequence> sequences;

   //---------------------- constructor ----------------------------------------
   /**
    * Constructor -- is the main code for the app
    */
   public FastaFile( String fileName )
   {
      readSequences( fileName );
   }
   //---------------------- get( int ) ----------------------------------------
   /**
    * return the i-th sequence or null
    */
   public DNASequence get( int i )
   {
      if ( sequences == null || i >= sequences.size() )
         return null;
      else
         return sequences.get( i );
   }
   //---------------------- size() ----------------------------------------
   /**
    * number of sequences in the file
    */
   public int size()
   {
      if ( sequences == null )
         return 0;
      else
         return sequences.size();
   }
   //---------------------- compositeLength() ----------------------------------------
   /**
    * the total length of all sequences in the file
    */
   public int compositeLength()
   {
      int len = 0;
      if ( sequences != null )
      {
         Iterator<DNASequence> iter = this.iterator();
         while ( iter.hasNext() )
            len += iter.next().length();
      }
      return len;
   }
   
   //---------------------- iterator() ----------------------------------------
   /**
    * return an iterator over the DNASequences
    */
   public Iterator<DNASequence> iterator()
   {
      return new SequenceIterator();
   }
   //--------------------------- findSequence( String ) ------------------------
   /**
    * Search in the sequences collection for a sequence with the requested id.
    * If found, return it, else return null.
    */
   public DNASequence findSequence( String id )
   {
      DNASequence found = null;
      for ( int i = 0; i < sequences.size() && found == null; i++ )
      {
         DNASequence seq = sequences.get( i );
         if ( seq.getId().equals( id ))
            found = seq;
      }
      return found;
   }
   //---------------------- getComposite() ------------------
   /**
    * Return a single string that is the concatenation of all sequences
    * in the fasta file, separated by the default separator.
    */
   public String getComposite()
   {
      return getComposite( "**********" );
   }
   
   //---------------------- getComposite( String ) ------------------
   /**
    * Return a single string that is the concatenation of all sequences
    * in the fasta file, separated by the parameter separator.
    */
   public String getComposite( String separator )
   {
      StringBuffer reference = new StringBuffer();
      Iterator<DNASequence> iter = this.iterator();
      while ( iter.hasNext() )
      {
         reference.append( iter.next().getDNA() );
         reference.append( separator );
      }
      reference.delete( reference.length() - separator.length(), reference.length() );
      return reference.toString();
   }
   //++++++++++++++++++++++ public internal class ++++++++++++++++++++++++++++
   public class SequenceIterator implements Iterator<DNASequence>
   {
      //------------------ instance variables ------------------------------
      private Iterator<DNASequence> vectorIterator = null;
      //-----------------  constructor ------------------------------------
      public SequenceIterator()
      {
         vectorIterator = sequences.iterator();
      }
      //------------------- hasNext() -----------------------------------
      public boolean hasNext()
      {
         return vectorIterator.hasNext();
      }
      //------------------- next() ---------------------------------------
      public DNASequence next()
      {
         return vectorIterator.next();
      }
      //------------------- remove() ---------------------------------------
      public  void remove()
      {
         vectorIterator.remove();
      }
   }
      
   //++++++++++++++++++++++ private utility methods ++++++++++++++++++++++++++++
   //------------------------ readSequences( String  ) --------------------------
   private void readSequences( String fileName )
   {
      sequences = new Vector<DNASequence>();
      
      // open a scanner for the fasta file -- and read the first header
      openScanner( fileName );      
      while ( nextHeader != null )
      {
         try
         {
            String header = nextHeader;
            String dna = readSequence();
            sequences.add( new DNASequence( header, dna ) ); 
         }
         catch ( DNASequence.DNASequenceException dnaEx )
         {
            System.err.println( dnaEx.getMessage() + "\nSequence input ignored." );
         }
      }
   }
   //------------------------ openScanner() -------------------------------
   private void openScanner( String fileName )
   {
      try
      {
         File inFile = new File( fileName );
         scanner = new Scanner( inFile );
         nextHeader = scanner.nextLine();
         if ( nextHeader.charAt( 0 ) != '>' )
         {
            System.err.println( "***Error: " + fileName + 
                               " does not start with '>' " );
            scanner = null;
            nextHeader = null;
         }            
      }
      catch ( IOException ioe )
      {
         System.err.println( "Unable to open file: " + fileName + "\n"
                               + ioe.getMessage() );
         scanner = null;
         nextHeader = null;
      }
      catch ( NoSuchElementException nse )
      {
         System.err.println( "***Error: " + fileName + " is empty. " );
         scanner = null;
         nextHeader = null;
      }
   }
   //------------------------ readSequence() -------------------------------
   private String readSequence()
   {
      String line;        // one line of input
      String dna = "";
      boolean moreToRead = scanner.hasNextLine();
      nextHeader = null;
      
      while ( moreToRead )
      {
         line = scanner.nextLine();
         if ( line.startsWith( ">" ))
         {
            nextHeader = line;
            moreToRead = false;
         }
         else
         {
            dna += line;
            moreToRead = scanner.hasNextLine();
         }
      }
      return dna;
   }
   //------------------------ printSequences() -------------------------------
   private void printSequences()
   {
      System.out.println( "++++++++++++++++++++++++++++++++++++++++++++++++++" );
      for ( int i = 0; i < sequences.size(); i++ )
         System.out.print( sequences.get( i ));
   }
}