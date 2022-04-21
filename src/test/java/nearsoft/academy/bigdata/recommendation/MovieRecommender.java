
package nearsoft.academy.bigdata.recommendation;

import java.util.zip.GZIPInputStream;
import java.io.*;
import java.util.*;

import org.apache.commons.cli2.option.Switch;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


/*

Read line by line
Excract the values you want (you need users, products and score/review)
 */

public class MovieRecommender {
    private int totalReviews;
    private int totalUsers;
    private int totalProducts;

    public int userIndex;
    public int productIndex;
    Map<String, Integer> productDict = new Hashtable();
    Map<Integer, String> productDictInverse = new Hashtable();
    Map<String, Integer> userDict = new Hashtable();

    public MovieRecommender(String dataset) throws IOException{

        this.totalReviews = 0;
        this.totalUsers = 0;
        this.totalProducts = 0;

        //Read the movies.txt.gz as binary, then as text and then prevent overflows
        GZIPInputStream gz = new GZIPInputStream(new FileInputStream(dataset));
        Reader reader = new InputStreamReader(gz);
        BufferedReader buffReader = new BufferedReader(reader);


        File emptyFile = new File("movies.csv"); //variable
        FileWriter fileWriter = new FileWriter(emptyFile);
        BufferedWriter writer = new BufferedWriter(fileWriter); //to controll data stream and avoid overflow

        String line; //Which line we are
        while ((line = buffReader.readLine()) != null) //go over all lines
        {
            if(line.contains("product/productId"))
            {
                String productId = line.split(" ")[1]; //variable with the product id
                if (productDict.containsKey(productId)) //If product already in dict
                {
                    productIndex = productDict.get(productId); //save its index, so we can access to it later
                }
                else //it isnt? is new?
                {
                    totalProducts = totalProducts + 1; //there is another product to count
                    productIndex = totalProducts; //and this is its new index
                    productDict.put(productId, totalProducts); //save new id in dict
                    productDictInverse.put(totalProducts, productId); //reverse the dict to use it in mahout
                }
            }
            else if (line.contains("review/userId"))
            {
                String userId = line.split(" ")[1];
                if(userDict.containsKey(userId))
                {
                    userIndex = userDict.get(userId); //the value of the id
                }
                else
                {
                    totalUsers = totalUsers + 1;
                    userIndex = totalUsers;
                    userDict.put(userId, totalUsers);
                }
            }
            else if (line.contains("review/score"))
            {
                totalReviews = totalReviews + 1; //add a review to count
                String scores = line.split(" ")[1];
                writer.write(userIndex + "," + productIndex + "," + scores + "\n"); //save text in csv to be read after

            }
        }
        buffReader.close(); //close buffReader to avoid over flow
        writer.close();
    }

    int getTotalReviews(){return this.totalReviews;}

    int getTotalProducts(){return this.totalProducts;}

    int getTotalUsers(){return this.totalUsers;}

    public List<String> getRecommendationsForUser(String userID) throws IOException, TasteException //Do mahout
    {
        DataModel model = new FileDataModel(new File("movies.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List <RecommendedItem> recommendations = recommender.recommend(userDict.get(userID), 3);
        List<String> listofrecommendations = new ArrayList<String>();


        for (RecommendedItem recommendation : recommendations)
        {
            int itemId = (int)recommendation.getItemID(); //id of recommendation
            String productId = productDictInverse.get(itemId); //ask for productId
            listofrecommendations.add(productId);
        }
        return listofrecommendations;
    }

}