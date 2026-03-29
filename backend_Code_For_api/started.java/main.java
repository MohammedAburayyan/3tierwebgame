import java.util.Scanner;
import java.util.Random;

public class main {
   public static String cleanRead(Scanner scanner){
      String response = scanner.nextLine();
      response = response.trim();
      response = response.toLowerCase();
      return response;
   }
   public static void say(String output) {
     System.out.println("================================================================================================================================================");
     System.out.println(output);
     System.out.println("================================================================================================================================================");
   }
   public static void main(String[] args) {
   Random rng = new Random();
     String playAgain;
     do{ 
     say("Hello traveler, which path do you choose, the path of salvation or the path of damnation");
      Scanner scanner = new Scanner(System.in);
      String response = cleanRead(scanner);
         while (!response.equals("salvation") && (!response.equals("damnation"))){
            say("Guess you dont speak my language, come back when you learn it!");
            response = cleanRead(scanner);
         }
         if (response.equals("salvation")) {
           say("You have chosen the path of " + response + " let the light guide you, (type option 1 : FIGHT THE LIGHT) (type option 2 : let the light take control...)");
            String pathA = cleanRead(scanner);
            if (pathA.equals("option 1") || pathA.equals("1")){
               int chance = rng.nextInt(100);
               if (chance < 70){
                 say("*you throw a punch at the light and it misses terribly* YOU DARE CHALLENGE ME!! I SENTENCE YOU TO ETERNAL DAMNATION!!!!!");
               }else if (chance >= 70){
                 say("*you throw a punch at the light and it acutally hits square in the chin* AHHHHHH IT BURRNNSS DAMNNN YOUUUUUUUUUUU...");
                 say("Dungeon Master: dude you got the luckiest roll now I have to readjust the story again!.... lets start from the beginning...");
               }
            }else if (pathA.equals("option 2") || pathA.equals("2")){
              say("*you allow the light into your soul* Ascend and become a true warior!!");
            }else{
              say("Must have gotten here by accident since you cant speak my language, BEGONE!");
            }
         }else if (response.equals("damnation")){
           say("You have chosen the path of " + response + " Die as if you had no right to live... unless you reeeaalllly regret your initial choice (type option 1 : SCREW YOUUUUUUUU) (type option 2 : FORGIVE MEEEEE)");
            String pathB = cleanRead(scanner);
            if (pathB.equals("option 1") || pathB.equals("1")){
              say("*you scream SCREW YOU... and it was very anticlimactic* wow that was.... a really dumb choice dude you could have lived... anyways SMYTE!");
            }else if(pathB.equals("option 2") || pathB.equals("2")){
              say("*you have no spine and beg for forgiveness* yea I thought so, plus I needed new warriors soooo.... ASCEND");
            }else{
              say("you must be very scared to jumble your words oh well.... SMYTE!");
            }
         }
        say("Play again? (yes or no)");
         playAgain = cleanRead(scanner);
         while (!playAgain.equals("yes") && !playAgain.equals("no")){
           say("Please use a valid response (yes or no)");
            playAgain = cleanRead(scanner);
         }
      }while (playAgain.equals("yes"));
   }
}