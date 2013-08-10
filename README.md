wikicategory
============

Gets all articles from a wikipedia category in wikitext format

This class can be used to get all articles from a wikipedia category and
write those files to disk in wikitext format.

Usage:

First get an WikiCategory object like this;

    WikiCategory wk = new WikiCategory("Yaşar Arabacı", "yasar11732@gmail.com", "http://en.wikipedia.org/w/api.php", "Category:Psychology");
    
Then run it;

    JSONObject pages = wk.run()
    
If you want to write those pages into seperate files, you can do so;

    wk.saveToFiles(".txt");
    
Files will be written to a directory name wiki in current working directory with file extension ".txt". You can also specify where to write them

    wk.saveToFiles(new File("path to directory"), ".txt");
    
javadoc documentation can be found at: http://yasar11732.github.io/wikicategory/
    
    
     
     
