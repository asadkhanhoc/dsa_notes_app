package com.asadullah.brainwave.model;

public class NotesLinkedList {
    private Note head;
    private int size;

    public NotesLinkedList() {
        head = null;
        size = 0;
    }

    public void insertNote(String title, String content, String url,String selectedImagePath, Long selectedDateTime ) {
        if (title == null || content == null) {
            throw new IllegalArgumentException("Title and content cannot be null");
        }

        // Create new note with the next reference initially set to null
        Note newNote = new Note(title, content, url,selectedImagePath,selectedDateTime );
        // Set the next reference to current head
        newNote.next = head;
        // Update head to point to new note
        head = newNote;
        size++;
    }

    public boolean deleteNote(String title) {
        if (title == null || head == null) return false;

        // Special case hai deleting head k liye
        if (head.title.equals(title)) {
            head = head.next;
            size--;
            return true;
        }

        // Search for note to delete
        Note current = head;
        while (current.next != null) {
            if (current.next.title.equals(title)) {
                current.next = current.next.next;  // Remove the note from the chain
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }
    public Note searchNote(String title) {
        if (title == null) return null;

        Note current = head;
        while (current != null) {
            if (current.title.equals(title)) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    public Note[] toArray() {
        Note[] array = new Note[size];
        Note current = head;
        int index = 0;
        while (current != null) {
            array[index++] = current;
            current = current.next;
        }
        return array;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
