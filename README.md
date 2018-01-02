# Image Manipulation Language Compiler

This compiler is developed to work with a language engineered for image manipulation. 
The compiler scans, tokenizes, parses and executes the input. It is capable of handling 
images of any dimensions, along with manipulating them with a simple and easy to understand syntax.

**A simple example:** <br /><br />
  ```
  image f[1024,1024];         // Declares an image variable f of size 1024 * 1024 
  i <- @0;                    // Reads an image from command line parameters at 0th position
  image g[1024,1024] = @1;    // Declares an image variable g of size 1024 * 1024
                                  from command line at 1st position 
  image i[1024,1024];         // Declares an image variable f of size 1024 * 1024 
  i[x,y] = f[x,y] + g[x,y];   // Iterates through every pixel of i, and sets its color value
                                  to the sum of (ith,jth) pixel in image f and g
  i -> SCREEN;                // Displays the image i on the SCREEN. 
````
The compiler uses an Abstract Syntax Tree to parse the syntax and embed the semantics of the language
via annotating the syntax tree. The AST is traversed twice using the Visitor Pattern. In the first pass, 
the tree is annotated for the purpose of type checking. In the second pass, the tree is annotated for 
the purpose of code generation. 

# Code Generation

To eliminate the need to build all components of code execution(linker, loader, assembler, etc), the solution
is developed using Java bytecode and the Java Virtual Machine. This allows us to leverage the benefits of 
the JVM, such as portability, security, etc. Whilst traversing the AST, upon visiting each node, we use the
ASM to generate the bytecode. The bytecode is then dynamically loaded and executed by the JVM. Command Line
Parameters can also be passed to the dynamic class. To implement our code, we emulate all of our code within
the main function of the dynamic class. Our language has a single global scope, hence, each variable is emulated 
as a static variable of the dynamic class. 

# Abstract Syntax Tree

An abstract syntax tree is a parse tree which is constructed during the parse phase of the compiler. It is then
annotated with attributes during the type checking and code construction phase. An AST consists of nodes, each node
representing elements of the input.

For example : 

int i = j + 1;

The above statement represents a Declaration_Variable Node. It consists of three attributes: 
Type - int
Identifier Name - i
Expression - Binary_Expression

Binary_Expression consists of the following three attributes:
Operator - +
Expression_Ident - j
Expression_IntLit - 1

These nodes are created as we parse the input and build the tree as we go along. In the type checking phase, we calculate the
types of the more basic nodes and pass it along to the parent nodes. The parent nodes inherit their type
infromation from the children nodes. The parent nodes check and confirm the types which are passed from 
the children. In the code generation phase, each node is visited and bytecode is generated from the
attributes stored in each node.
