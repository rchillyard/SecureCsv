# SecureCsv
Application to read/write encrypted Csv files.

SecureCsv is a main program driven by command line arguments.
For details on issues of command line syntax, please see 
[https://github.com/rchillyard/Args](https://github.com/rchillyard/Args).

The library for reading/writing the Csv files is [https://github.com/rchillyard/TableParser](https://github.com/rchillyard/TableParser).

For simplicity, SecureCsv supports only RawRow-based tables.
That isn't usually a problem because the purpose of SecureCsv is to primarily to
create encrypted files and allow others to decrypt selected rows of the file for display purposes.
If you need all of the functionality provided by typed rows, then use TableParser.

An example command line for invoking the workflow that reads a plaintext Csv file
and writes it encrypted to another file is as follows:

    -a encrypt -f examples/TeamProject.csv -o output/TeamProjectEncrypted.csv -n 2

Here, the action is "encrypt,"
the input filename is "examples/TeamProject.csv,"
the output filename is "output/TeamProjectEncrypted.csv,"
and the number of header rows is 2.

In this case, the console output from the program will give the raw keys (passwords)
for the various rows.
You will need to make a note of these.

Another example, which invokes a workflow to decrypt a file is as follows:

        -a decrypt -f examples/TeamProjectEncrypted.csv -o output/TeamProject.csv -r 5 -p JOvJCG3sSrZHAdv3 -n 2

In this case, we decrypt the given CSV file.
However, our key (password) is only valid for one row, the one whose identity (the first column) is "5".

Other options:
* -m: will use "multiline mode" if it appears (no value necessary): this allows text to have embedded newlines;
* -d delimiter: allow for a different delimiter on input/output (default is comma).
* -k key column: specifies which column should be used as the row identifier (defaults to the first column)


Version 0.0.2: added mechanism for specifying the key column.

Version 0.0.1: initial version used to send out project data for Fall 2021.