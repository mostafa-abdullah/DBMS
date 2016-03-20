# Readme

The project has three packages
- DB
- BPTree
- Exceptions

#
#### DB Package:
Contains the basic files for the DBMS
- __DBApp.java__, this file contains all the major methods for the DBMS, insertIntoTable, selectFromTable, createIndex, update and delete.
- __DBAppTest.java__, this file contains the only main method in the project.
- __Page.java__, this file contains the class for creating the table pages.
- __Table.java__, this file contains the class for creating DB tables.
- __Tuple.java__, this file contains the class for creating single taple records stored in the pages.

#### BPTree Package
Contains the files of the B+ Tree
- __BPTree.java__, the main class for the B+ Tree, it contains the major methods: insert, delete, update, find.
- __Leaf.java__, the class for creating the tree leaves.
- __NonLeaf.java__, the class for creating the tree non-leaves.
- __Node.java__, the superclass which Leaf and NonLeaf classes extend.
- __NodeEntry.java__, the class for the content of the non-leaves.
- __TuplePointer.java__, the class for the content of the leaves.

#### Exceptions Package
- Contains the needed exceptions in the DBMS.
#
## How to run the project?
Using the makefile:
- Open the terminal and type the appropriate make command:
    - make all      --> compiles and runs the main method in DBAppTest class
    - make compile  --> compiles the files only and saves the .class files in the bin folder.
    - make run      --> runs the .class files from the bin folder.
    - make clean    --> Deletes the current data of the database


> To test the project, you need to add your custom database entries in DBAppTest.java, then use the makefile to run the project as indicated above, or run it directly from eclipse.

