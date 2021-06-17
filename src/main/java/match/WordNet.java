package match;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;

class WordNet {

    public static void main(String[] args) {
        //this system property sets the location of the wordnet dictionary. Move it to your needs
        System.setProperty("wordnet.database.dir","C:\\Program Files\\WordNet\\2.1\\dict\\");
                WordNetDatabase db = WordNetDatabase.getFileInstance();
        //file is the location of the list of words (newline separated) to be defined
        //a good place to get this is grep and a UNIX dictionary
        String file =
                "C:\\Documents and Settings\\Erik " +
                        "Price\\Desktop\\Downloads\\dictionary.txt";

        Synset[] syn;

        String[] usage;
        //array of strings for usage examples

        String temp = "";
        BufferedReader in = null;
        try {
            DataOutputStream out = new
                    DataOutputStream(new
                    FileOutputStream("C:\\Documents and Settings\\Erik Price" +
                    "\\Desktop\\Downloads\\out.txt"));
            //change this to where ever you want your output to be
            in = new BufferedReader(new FileReader(file));
            while((temp = in.readLine()) != null){
                /*what this entire while loop does:
                 *reads the file line by line and then
                 *saves it in the variable temp
                 *it then finds the definitions for the word
                 *and writes them to the file*/
                syn = db.getSynsets(temp);
                System.out.println("read: " + temp);
                for(int i = 0; i < syn.length; i++)
                {
                    out.writeBytes(temp + " - "); //write [word] -
                    System.out.println("Retrieved definition: "+
                            syn[i].getDefinition());
                    out.writeBytes(syn[i].getDefinition());
                    //write definition to file
                    usage = syn[i].getUsageExamples();
                    if(usage.length > 0) //if an example usage exists
                    {
                        out.writeBytes("ntExample Usage: "); //write it to a file
                        for(int j = 0; j < usage.length; j++)
                        {
                            System.out.println("Example usage: " + usage[j]);
                            //and keep writing it while there are more
                            out.writeBytes(usage[j]+" ");
                        }
                    }
                    //Windows endline. Change to your needs
                    out.writeBytes("rn");
                }
            }
        } catch (Exception ex) {
            System.out.println("Error: " + ex +"nExiting...");
            System.exit(0);
        }


    }
}
