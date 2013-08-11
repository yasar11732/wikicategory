wikicategory
============

Gets all articles from a wikipedia category in wikitext format

This class can be used to get all articles from specified category
from a [mediawiki](http://www.mediawiki.org/wiki/MediaWiki) powered site in a mediatext format and allows you
to save articles in text files. This class is hopefully usefull for
data-mining on mediawiki powered sites.

Usage:

First get an WikiCategory object like this;

    WikiCategory wk = new WikiCategory("Yaşar Arabacı", "yasar11732@gmail.com", "http://en.wikipedia.org/w/api.php", "Category:Psychology");
    
Then run it;

    JSONObject pages = wk.run()
    
If you want to write those pages into seperate files, you can do so;

    wk.saveToFiles(".txt");
    
Files will be written to a directory named wiki in current working directory with file extension ".txt". You can also specify where to write them

    wk.saveToFiles(new File("path to directory"), ".txt");
    
javadoc documentation can be found at: http://yasar11732.github.io/wikicategory/
    
    
     
     
