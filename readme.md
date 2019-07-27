### Large file sorter

Sorts large file lexicographically.
Use program arguments to specify file and chunk size in kilobytes. E.g. `"c:\temp\generated.txt" 20000`


Algo:
1. Split file into chunks of specified size
1. Sort lines inside chunks
1. Use k-way merge to merge lines from chunks to result file

### Large file generator

Use program arguments to specify lines count, max line length and file path. 
E.g. `100 100 "c:\temp\generated.txt"`
