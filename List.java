/**
 * A generic List implementation using a protected inner Node class.
 * Version 2: March 10, 2009
 *            Added previous() method
 *  Edited: rdb 04/07/09
 *     Implementation of previous() method was not correct. If _cur is head,
 *        next should become head and cur should become null; old code
 *        did not change cur or next. In this way could not insert at the
 *        head of the list since could not get the head to be next.
 * 
 * This version implements semantics that defines both a "next" and "current" 
 * node in the list, even when deletions occur. We want a program to be able to
 * traverse a list and delete nodes in the list as part of the traversal, 
 * without compromising the integrity of the traversal. This is done by 
 * defining the semantics of remove() to mean that the "current" node is
 * to be deleted. If this happens, we need to guarantee that access to the
 * next node (using the next() method) must work correctly even if the 
 * preceding node is deleted (or many preceding nodes, for that matter).
 * 
 * In principle, the "current" node is the one most recently added or accessed
 * by the application using add, first, next, or find methods.  In the absence
 * of "remove" operations, the "current" node is always the one that precedes 
 * the "next" node. If there is no next node, the current node should be the
 * tail node. The semantics of remove must maintain this model.
 *
 * remove() - removes the current node and sets the current node to be the one
 *            that precedes the next node, if there is one. If there is no
 *            node that precedes the next node, the next node is the head of
 *            the list; since we do not want remove to change the next node, 
 *            we set the current node to be undefined. (The alternative is to 
 *            have the current and next be the same node; this creates some
 *            implementation and consistency issues.)
 * remove( key ) - first sets the current node to the first node matching
 *            key, sets the next node to the following node (if there is one)
 *            and then executes remove().
 * add( item ) - now inserts the new item after the current node (before the
 *            next node) and the new node becomes the current node. This is
 *            equivalent to adding at the end of the list if you just 
 *            create the list and add, add, add, etc. 
 * 
 * public interface:
 *   List()           -- create an empty list
 *   T first()              -- return the data from the first node on list
 *                             and set "current" item to the head node.
 *                             if list is empty, returns null;
 *   T last()               -- returns the data in the last node on the list
 *                             and set "current" node to the tail node.
 *   T next()               -- returns the "next" item on the list; this node
 *                             becomes the "current" one. If there 
 *                             is no next entry, null is returned and "current"
 *                             becomes null.
 *   T previous()           -- returns the node preceding the current node, 
 *                             if there is one. The current node becomes the
 *                             next node and the returned node becomes the
 *                             current one, 
 *   T current()            -- returns the data in the current node; if the
 *                             current node is undefined, null is returned
 * 
 *   void add( T )          -- adds T object at the "current" position on
 *                             the list. If "current" position is undefined,
 *                             the new node will be added in front of the
 *                             "next" node. The "next" node is not changed.
 *                             This node will be the "current" node.                             
 *   void addHead( T )      -- adds T object to the head of the list, the
 *                             new node becomes the current position;
 *                             the next node is the one following the head.
 *   void addTail( T )      -- adds T object to the tail of the list; the
 *                             new node becomes the current position; the
 *                             "next" node is null.
 * 
 *   T    remove()          -- removes the "current" node, returns its data 
 *                             object. The current node becomes the one 
 *                             preceding the next node if the next node has
 *                             a preceding node. If not, "current" becomes
 *                             undefined and a subsequent remove() returns
 *                             null -- even though there are more nodes on 
 *                             the list.
 * 
 *   T    remove( String )  -- looks for an object whose key value matches
 *                             the passed string. if found, the object is 
 *                             removed from the list and its data returned.
 *                             The current node becomes undefined; the
 *                             "next" node is the one following the one removed.
 *   T    find( String )    -- finds the first node whose key value matches the
 *                             passed string. If no match occurs, return null.
 *                             If a match is found, return the data from the
 *                             node. The matched node becomes the "current" one 
 *                             and the following  node becomes the "next" one.
 *   T    get( int i )      -- returns the data in i-th node on list or null 
 *                             if there is no i-th node.
 *                             This node becomes the current one, and the one
 *                             following it becomes the next one.
 * 
 * boolean ifEmpty()        -- returns true if the list is empty.
 *   int     size()         -- returns the size of the list
 * void    clear()          -- remove all entries in the list
 * 
 * @author rdb
 * March 1, 2009
 */

public class List<K, T extends Comparable<K>>
{
   //----------------- instance variables --------------------------
   protected Node<T> _head;
   protected Node<T> _tail;
   protected Node<T> _cur;
   protected Node<T> _next;
   protected int     _size;
   
   //---------------- constructor ----------------------------------
   /**
    * create an empty linked list
    */
   public List() 
   {
      clear();
   }
   //----------------- clear() --------------------------------
   /**
    * remove all entries from the list
    */
   public void clear()
   {
      _head = null;
      _tail = null;
      _cur  = null;
      _next = null;
      _size = 0;
   }
   //----------------- T first() --------------------------------
   /**
    * return the object that is at the start of the list and set the
    * "current" node to the head of the list. Returns null if there are 
    * no nodes on the list.
    */
   public T first()
   {
      if ( _head == null )
         return null;
      else
      {
         _cur = _head;
         _next = _cur.next;
         return _cur.data;
      }
   }
   public T last()
   {
      if ( _tail == null )
         return null;
      else
      {
         _next = null;
         _cur = _tail;
         return _cur.data;
      }
   }
   //----------------- T current() --------------------------------
   /**
    * If the current node is defined, return the data in that node,
    * else return null.
    */
   public T current()
   {
      if ( _cur == null )
         return null;
      else
         return _cur.data;
   }
   //----------------- T peek() --------------------------------
   /**
    * Return the data in the next node without updated current or next
    */
   public T peek()
   {
      if ( _next == null )
         return null;
      else
         return _next.data;
   }
 
   //----------------- T next() --------------------------------
   /**
    * Move the "current" node position to the next node on the list
    * and return the  object that is the data for this node. 
    * Returns null if there is no next node and the "current" node
    * position is set to null.
    */
   public T next()
   {
      
      if ( _next == null  )
         return null;
      else
      {
         _cur = _next;
         _next = _next.next;
         return _cur.data;
      }
   }
   
   //----------------- T previous() --------------------------------
   /**
    * Move the "current" node position to the previoius node on the list
    * and return the  object that is the data for this node. 
    * The next node is updated to be the current one.
    * Returns null if there is no previous node. This means the current
    * node was the head, so current should become null and head should 
    * be the next.
    */
   public T previous()
   {
      if ( _cur == null )
         return null;
      Node<T> prev = findPrevNode( _cur );
      _next = _cur;
      _cur  = prev;
      if ( _cur != null )
         return _cur.data;
      else 
         return null;
   }
   
   //----------------- isEmpty( ) -------------------------------
   /**
    * returns true if the list is empty
    */
   public boolean isEmpty()
   {
      return _head == null;
   }
   //----------------- toString( ) -------------------------------
   /**
    * creates a string representation of the entire list. Only works for
    * very short lists; but it can be a useful debugging tool.
    */
   public String toString()
   {
      Node walk = _head;
      String str = "";
      while ( walk != null )
      {
         str += walk.data + ", ";
         walk = walk.next;
      }
      return str;
   }
   //----------------- toString( int ) -------------------------------
   /**
    * creates a string representation of the first and last k nodes
    * of the list.
    */
   public String toString( int k )
   {
      Node walk = _head;
      String str = "[ ";
      int    i   = 0;
      while ( walk != null && i < k )
      {
         str += walk.data + ", ";
         walk = walk.next;
         i++;
      }
      if ( i >= k )
      {
         int endStart = this.size() - k;
         if ( endStart > i )
            str += "..., ";
         else
            endStart = i;
         
         // We want the printing to leave the state of the List unchanged
         //   So we need to save the cur and next references while we
         //   peel off the end part. 
         Node<T> saveCur = _cur;
         Node<T> saveNext = _next;
         
         for ( int j = endStart; j < this.size(); j++ )
         {
            T item = this.get( j );
            if ( item == null )
               System.out.println( endStart + " " + j + " " + this.size() );
            str += this.get( j ) + ", ";
         }
         _cur = saveCur;
         _next = saveNext;
      }
      // change last , to ]
      if ( str.length() > 2 )
         str = str.substring( 0, str.length() - 2 ) + " ]";
      else
         str += "]";

      return str;
   }
   //----------------- size( ) -------------------------------
   /**
    * returns the size of the list
    */
   public int size()
   {
      return _size;
   }
   //----------------- add( T ) -------------------------------
   /**
    * add a node before the "next" node. This becomes
    * the current node, the next node is unchanged.
    */
   public void add( T newOne )
   {
      Node<T> temp = new Node<T>( newOne, null );
      _size++;
      if ( _head == null )
      {
         _head = _tail = _cur = temp;
         _next = null;
      }
      else if ( _next == null ) 
      {
         // we're adding at the tail
         _tail.next = temp;
         _tail = temp;
         _cur  = temp;
      }
      else
      {
         // we'll put the new information in the node
         //   referenced by next and point the information in the
         //   next node will go in the new node AFTER where
         //   next now is, then update next to the new node
         // first. put all of nexts fields into temp
         temp.data = _next.data;
         temp.next = _next.next;
         
         // now put new information into the node referenced by _next
         //   and set its next field to the new node
         _next.data = newOne;
         _next.next = temp;
         
         // Finally, the new data (which is in the old next node)
         //   should be _cur
         _cur = _next;
        
         // And, update _next to point at the new Node, which has
         //   the same data that the old _next used to have.
         if ( _tail == _next )
            _tail = temp;
         _next = temp;
      }
      /**** debugging code
      int checkSize  = computeSize();
      if ( checkSize != _size )
      {
         System.out.println( "*** List.add(s) internal error. size error."
                            + "Should be: " + _size + "    is: " + checkSize
                            + "  while adding: " + newOne );
         _size = checkSize;
      }
      if ( _next == null && _cur != _tail )
         System.out.println( "add: next null, tail not cur ");
      debugPrint( "After Add" );
      
      /************************************/
   }
   //----------------------- debugPrint ------------------------------------- 
   /**
    * print the key instance variables for debugging
    */
   private void debugPrint( String title )
   {
      System.out.println( "------------ "+ title + "--------------" );
      if ( _cur != null )
         System.out.println( "cur: " + _cur.data );
      else
         System.out.println( "cur: null" );
      if ( _head != null )
         System.out.println( "head: " + _head.data );
      else
         System.out.println( "head: null" );
      if ( _tail != null )
         System.out.println( "tail: " + _tail.data );
      else
         System.out.println( "tail: null" );
      if ( _next != null )
         System.out.println( "next: " + _next.data );
      else
         System.out.println( "next: null" );
   }
   //----------------- addHead( T ) -------------------------------
   /**
    * adds the data to a node that becomes the new list head.
    * The current node becomes the new head; the next node
    * becomes the one following the new head.
    */
   public void addHead( T newOne )
   {
      _cur = _head = new Node<T>( newOne, _head );
      _size++;
      _next = _cur.next;
      if ( _tail == null )
         _tail = _head;
   }
   //----------------- addTail( T ) -------------------------------
   /**
    * adds the data to a node that becomes the new list tail.
    */
   public void addTail( T newOne )
   {
      _cur = new Node<T>( newOne, null );
      _size++;
      if ( _head == null )
         _head = _cur;
      else
         _tail.next = _cur;
      _next = null;
      _tail = _cur;
   }
   //----------------- remove() ----------------------------------
   /**
    * remove the current node of the list, if there is one
    * 
    * update current node to be the preceder of the next node, if
    * there is one. Otherwise current becomes undefined
    */
   public T remove()
   {
      if ( _head == null )
         return null;
      if ( _cur == null )
      {
         // It is possible for this condition to occur; it does not need
         // an output message, but I put one in for debugging mainly.
         System.out.println( "List.remove(): no current node defined." );
         return null;
      }
      T  retObj = _cur.data;
      
      Node<T> prev = findPrevNode( _cur );
      
      if ( prev == null )  // _cur is the head
      {
         if ( _next == null ) // nothing else on list
            _head = _tail = _cur = null;
         else
            _head = _next; // cur will be set to null below
      }
      else // _cur is not the head
      {
         prev.next = _next;    // node before cur now points to next        
         if ( _cur == _tail ) // if cur was the end, need to update end
            _tail = prev;     // _cur == tail => _next == null & prev.next, too
      }
      
      _cur = prev;      // In all cases, _cur prev, which might be null
      _size--;
      
      /*********************  Debug tests
      if ( _cur.next != _next )
         System.err.println( "****List.remove(): Internal error " +
                             "current does not precede next!" );
      if ( _next == null && _cur != _tail && _cur != null)
      {
         System.err.println( "****List.remove(): Internal error " +
                             "next is null, but cur isn't tail " + _cur.data + " " + _tail.data );
      }
      if ( _size == 0 && ( _head != null || _tail != null ))
         System.out.println( "***List.remove internal error: " 
                             + " size 0, head or tail not null!" );
      int checkSize  = computeSize();
      if ( checkSize != _size )
      {
         System.out.println( "*** List.remove internal error. size error."
                            + "Should be: " + _size + "    is: " + checkSize
                            + "  while Removing: " + retObj );
         _size = checkSize;
      }
      debugPrint( "After remove" );
      /***********************************************/

      return retObj;
   }
   //----------------- remove( K ) -----------------------------
   /**
    * remove and return the data from the first entry on the list whose
    * key matches the argument. 
    * 
    * the next node becomes the one following the removed node.
    * The current node becomes the one preceding the next one, if
    * there is a preceder, else it becomes undefined.
    */
   public T remove( K key )
   {
      T       retObj = null;
      Node<T> prev = null;
      Node<T> chase = _head;
      while ( chase != null && chase.compareTo( key ) != 0 )
      {
         prev = chase;
         chase = chase.next;
      }
      if ( chase != null )  // remove node from the list and return the object
      {
         if ( prev == null )    // found node is head of list
            _head = chase.next;
         else                      // make previous node now reference 
            prev.next = chase.next; // the node referenced by the found node
         if ( chase == _tail )
            _tail = prev;
         retObj = chase.data;
         _next = chase.next;
         _cur = prev;
         _size--;
      }
      if ( _head == null )
         _tail = null;
      /*********************** debug tests ***************************
      int checkSize  = computeSize();
      if ( checkSize != _size )
      {
         System.out.println( "*** List.remove(s) internal error. size error."
                            + "Should be: " + _size + "    is: " + checkSize
                            + "  while Removing: " + key );
         _size = checkSize;
      }
      debugPrint( "After remove( key )" );
      /***************************************************************/
      return retObj;
   }
   //------------------ computeSize() -----------------------------
   /**
    * A debugging method to cross check the _variable with the number of
    *  entries you get when following the links.
    */
   private int computeSize()
   { 
      Node<T> chase = _head;
      int size = 0;
      while ( chase != null )
      {
         size++;
         chase = chase.next;
      }
      return size;
   }
   //----------------- findPrevNode( Node ) ------------------------
   /**
    * search the list until find the node that precedes the argument node.
    */
   private Node<T> findPrevNode( Node<T> node )
   {
      if ( node == null || _head == node )
         return null;
      
      Node<T> chase  = _head;
      while ( chase != null && chase.next != node )
         chase = chase.next;
      return chase;
   }      
   //----------------- find( K ) -----------------------------
   /**
    * find an entry whose key matches the argument K. If found, return
    * the data for the entry. If not found, return null.
    */
   public T find( K key )
   {
      Node<T> chase = _head;
      while ( chase != null && chase.compareTo( key ) != 0 )
         chase = chase.next;
      if ( chase == null )
         return null;     // not found
      else
      {
         _cur = chase;
         _next = chase.next;
         return chase.data;
      }
   }
   //--------------------- get( int ) --------------------------------------
   /**
    * get n-th entry on the list
    */
   public T get( int n )
   {   
      Node<T> chase = _head;
      
      for ( int i = 0; i < n && chase != null; i++ )
         chase = chase.next;
      
      if ( chase == null )
         return null;
      else
      {
         _cur = chase;
         _next = chase.next;
         return chase.data;
      }
   }   

   //++++++++++++++++++ Node inner class ++++++++++++++++++++++++++++++++
   /**
    * The Node class is hidden from the application, but is accessible
    * to child classes.
    * 
    * Nodes can take data objects (T) as long as they implement the 
    * Comparable<String> interface. (It's a bit awkward, but it is necessary
    * to say that T "extends" Comparable<String>, even though it really
    * just "implements" it.)
    * 
    * The instance data members are public since this class is only
    * available inside the List and its children.
    */
   protected class Node<T extends Comparable<K>> 
   {
      public Node<T> next = null;
      public T data = null;
      
      public Node( T newData, Node<T> n )
      {
         data = newData;  next = n;
      }

      public int compareTo( K key ) 
      {  
         return data.compareTo( key ); 
      } 
   }
}
