// CS1 project 3: Hangman
//Written by Ruoying Hao, Alexis Gonzalez.
//I have acted with honest and integrity in producing this work and am unaware of anyone who has not.

import java.io.*;//import this package to read in the dictionary file.
import java.util.*;//import this package to create random, scanner.

public class Hangman {
	//create constants and arrays.
	private static final int WORDLENGTH = 5;//the length of a word is always 5.
	private static final int DICTIONARYSIZE = 12478;//the size of the dictionary needs to be changed if wordList.txt changes.
	private static String[] dictionary = new String[DICTIONARYSIZE]; //dictionary array store every word in the dictionary file.
	private static boolean[] inPlay = new boolean[DICTIONARYSIZE]; //when inPlay[i] is false, dictionary[i] may be a word that is still in play so far.
	private static char[] grid= {'?','?','?','?','?'};  //this array is for the hangman grid.
	private static char[] lettersAndMarks;//creates an array that will store the incorrect letters and question marks, the size is unknown.
	private static boolean[] mark=new boolean[DICTIONARYSIZE];//if mark[i] is true, it means we have test dictionary[i],and dictionary[i] is not in play, we won't consider that word anymore.


	public static void main(String[] args) throws FileNotFoundException{
		readDictionary(); //reads in dictionary.
		Scanner console = new Scanner(System.in);//creates a scanner that enables java to interact with the player.
		char guessLetter;//the most recent letter that the player have guessed.
		int numwithletter;//the number of words that are in play and have a particular letter.
		String assumeAnswer="";//the temporary answer that is randomly generated, "hiding" beneath the question marks.
		int haveGuessed;//the amount of times that the player have guessed wrong.
		int totalTested;//the number of words have a particular letter, but the letter is not in the answer so these words will not be in play.
		int guessTimeTotal;//the chances of incorrect letter guess that the player will receive.
		int netSize;//the size of the words that is still in play.
		boolean win=true;//Don't worry, win will be set to false every time the player choose to start a new game.
		while (yesTo("Do you want to play Hangman?", console)){//enter the while loop if the player choose to start a game.
			win=false;//set win to false in preparation for appropriate feedback.
			
			//puts all the words into in play list. This need to be done to prepare for a new game after finishing a game.
			for(int i=0;i<DICTIONARYSIZE;i++){
				inPlay[i]=false;
				mark[i]=false;
			}
			//resets the grid into question marks. This need to be done to prepare for a new game after finishing a game.
			for(int n=0;n<WORDLENGTH;n++){
				grid[n]='?';
			}
			haveGuessed=0;
			totalTested=0;
			netSize=DICTIONARYSIZE-totalTested;//it actually equals DICTIONARYSIZE.


			guessTimeTotal=getGuessTime(console);//the input won't be stored as guessTimeTotal if the input is not appropriate(too big, to small, not an integer).

			//store appropriate number of question mark after receiving the total guessing time.
			lettersAndMarks=new char[guessTimeTotal];
			for(int i=0;i<guessTimeTotal;i++){
				lettersAndMarks[i]='?';
			}
			//java will keep examining the letter until the player uses up all the guesses or the player wins.
			while(haveGuessed<guessTimeTotal &&win==testIfWin()){
				System.out.print("The incorrect letters guessed so far are: ");//prints out message for the player.
				printLettersAndMarks(guessTimeTotal);//prints out the wrong letters and question marks/
				guessLetter=getGuessLetter(console).charAt(0);//switches the String to char.
				numwithletter=numWithLetter(guessLetter);
				netSize=DICTIONARYSIZE-totalTested;//netSize is constantly changing, depends on 
				double percentage=numwithletter/(double)netSize;//one of the variables among numwithletter and netSize should be double in order to calculate percentage as a double. 
				//if the letter is in the answer:
				if(testPercentage(percentage)){
					//update inPlay array according to mark array.
					cleanup();
					assumeAnswer=assumeWordCorrect(numwithletter);//generates a temporary answer that contains that letter.
					locateRightLetter(assumeAnswer,guessLetter);//reveals location of the correct letter in the hangman grid.
					letterCorrect(guessLetter);//prompts a message to tell the player the guess is correct.
					totalTested=DICTIONARYSIZE-numwithletter+narrow(guessLetter);//update the number of words that are not in play anymore. 
					                                                               //This include the words that don't have the correct letter
				}                                                                    //as well as the words that have that letter but not in the right location.
				//if the letter is not in the answer:
				else{
					//update the mark array.
					mark();
					assumeAnswer=assumeWordWrong(netSize,numwithletter);//generates a temporary answer that do not contain that wrong letter.
					letterWrong(guessLetter);//prompts a message to tell the player the guess is incorrect.
					totalTested+=numwithletter;//update the number of words that are not in play anymore 
					                            //by adding the number of words that used to be in the inPlay list contain the wrong letter.

					//according to the sample in the instruction, successfully guessing out a right letter don't take up a guess.
					 //so the following statements only exist when the player guessed wrong. 
					haveGuessed++;
					incorrectSoFar(haveGuessed,guessLetter); 
				}
			}
			win=testIfWin();//assigns boolean value to win.
			feedback(haveGuessed,win,assumeAnswer);//after a game ends, tell the player the correct answer and whether the player wins.
		}
		replyNo(win);//gives appropriate message if the player don't want to play the game (any more).
	}


	// returns the number of words in the dictionary that are in play that have a particular letter
	public static int numWithLetter(char letter){
		int count = 0;//this is the number of words that are in play and have that letter
		for(int i=0;i<=DICTIONARYSIZE-1;i++){ //goes through each word in the dictionary
			int countInOneWord=0; //the number of times the letter appears in word
			if(inPlay[i]==false){//examining the words that are in play only.
				for(int n=0; n<=dictionary[i].length()-1;n++){ //tests every letter in the word.
					if(dictionary[i].charAt(n)==letter){
						countInOneWord++;
					}
				}
				if(countInOneWord>0){// if there is at least one letter is the guessed letter, update count and take the word out of in play list.
					count++;
					inPlay[i]=true;//Since most of the time the guessed letter is not in the answer, which means the word that contains the guessed letter will not be in play later,
				}                    //we set inPlay to true at first(we take them out of the in play list). A small amount of them will be put into the in play list again later.
			}
		}
		return count;
	}	

	// reads the dictionary from the file into the dictionary array
	public static void readDictionary() throws FileNotFoundException{
		File f= new File("wordList.txt");//creates a file object.
		Scanner input=new Scanner(f);//creates a scanner that read the file.
		//read the words into the array.
		for(int i=0;i<DICTIONARYSIZE;i++){
			dictionary[i]=input.next().toLowerCase();
		}
		input.close();
	}

	//utility function to ask user yes or no
	//No modifications are necessary for this method.
	//It uses a forever loop -- but the loop stops when something is returned.
	public static boolean yesTo(String prompt, Scanner console) {
		for (;;) {//this is pretty interesting
			System.out.print(prompt + " (y/n)? ");
			String response = console.next().trim().toLowerCase();//trim() and toLowerCase() make it case insensitive.
			if (response.equals("y"))// if the player want to start a new game, enter the while loop
				return true;
			else if (response.equals("n"))//if the player do not want to start a new game, do not enter the while loop.
				return false;
			else
				System.out.println("Please answer y or n.");//after printing out this message, return to the prompt.
		}
	}

	//The following three methods are used to test if the player entered an appropriate integer for total guessing time.
	//the structure is inspired by NumberGuess3 class, a robust number guessing game we finished on CS class.
	//there may be a easier way to accomplish this task.
	//when the player input something, first test if it is an integer(level 1), next test if the integer is too big(level 2), finally test if the integer is too small(level 3).

	//this is level 3
	public static int getGuessTime(Scanner console) {
		int guess = getIntLimit(console);//this statement pass on the input to level 2.
		while (guess <= 0) {//a player cannot guess zero times or negative times.
			System.out.println("Please enter an integer that is bigger than 0");
			guess = getIntLimit(console);//passes on the input to level 2(we start again, since the player can enter another invalid input)
		}
		return guess;//if the input is an integer and is not too big or too small, return it.
	}
	//level 2
	public static int getIntLimit(Scanner console){
		int guess=getInt(console, "How many guesses do you want?");//this statement pass on the input to level 1.
		while(guess>20){//look, even a dummy can get the right answer if he/she asks for millions of chances.
			System.out.println("You tried to guess too many times, which makes the game unchallenging. Please enter a smaller number.");
			guess=getInt(console, "How many guesses do you want?");//passes on the input to level 1.
		}
		return guess;//if the input is an integer and is not too big, return it.
	}
	//level 1
	public static int getInt(Scanner console, String prompt) {
		System.out.println(prompt);//prompt the player to enter guess time.
		//If the player didn't enter an integer,ask the player to enter a new one.
		while (!console.hasNextInt()) {
			console.next();//discard the input
			System.out.println("Please enter an integer.");
			System.out.println(prompt);
		}
		return console.nextInt();
	}

	//The following three methods are used to test if the player entered an appropriate letter.
	//the structure is inspired by NumberGuess3 class, a robust number guessing game we finished on CS class.
	//there may be a easier way to accomplish this task.
	//when the player input something, first test if it is a number(level 1), next test if the input contains more than one character(level 2), 
	//finally test if the input is a symbol(level 3).

	//level 3. robust if the player enters an symbol(say "+"), but this code do not robust when the player enters an letter that is examined, 
	//it will end up taking up a guess(ie: entering "a" twice).
	public static String getGuessLetter(Scanner console){
		String guess=getGuessLetterLimit(console);//passes on the input to level 2.
		//At this point guess.charAt(0) can never be an number, so guess can only be a symbol when its unicode is smaller than a or bigger than b
		while(guess.charAt(0)<'a'||guess.charAt(0)>'z'){
			System.out.println("Please enter a letter instead of a symbol.");
			guess=getGuessLetterLimit(console);//passes on the input to level 2
		}
		return guess;
	}
	//level 2
	public static String getGuessLetterLimit(Scanner console){
		String guess = getLetter(console,"Guess your next letter:");//passes on the input to level 1.
		//We set the max length of input as 1 because it is easier to handle.
		while (guess.length()>1 ) {
			System.out.println("Please enter one letter at a time");
			guess = getLetter(console,"Guess your next letter:");//passes on the input to level 1.
		}
		return guess;
	}

	//level 1
	public static String getLetter(Scanner console, String prompt) {
		System.out.println(prompt);//prompt the player to guess a letter.
		//if the input is a number, discard the input and prompt the player to enter a new one.
		while (console.hasNextInt()) {
			console.nextInt();
			System.out.println("Please enter a letter instead of a number");
			System.out.println(prompt);//fence post
		}
		return console.next().trim().toLowerCase();//makes it case insensitive.
	}

	//tests if a guessed letter is in the answer.
	//(If at least 20% of the words in play do not contain that letter, the letter is not in the word)
	//=(If less than 80% of the words in play contain that letter, the letter is not in the word)
	//=(if at least 80% of the words in play contain that letter , the letter is in the word)
	public static boolean testPercentage(double percentage){
		if(percentage>=0.8){
			return true;
		}else
			return false;
	}

	//prompts a message to tell the player the guess is correct.
	public static void letterCorrect(char guessLetter){
		System.out.print("Lucky! there is an "+guessLetter+". Your hangman grid is: ");
		printGrid(); //call the method that prints out the grid with the correct letter revealed in the correct spot(s).
	}
	//prompts a message to tell the player the guess is incorrect.
	public static void letterWrong(char guessLetter){
		System.out.print("Sorry, there is no "+guessLetter+". Your hangman grid is: ");
		printGrid(); //call the method that prints out the grid with no update.
	}

	//Randomly generates a temporary answer among the words in play that contain the correct letter.
	public static String assumeWordCorrect(int numwithletter){
		Random r=new Random();//create a random object for generating temporary answer.
		int i=0;//i is related to the random answer's index. 
		//ie: i=4 means in the dictionary array,among the words that in play, the forth word that contains that correct letter is the temporary answer.
		int j=0;// j is the index of dictionary array,regardless whether that word is in play.
		int randomAnswerIndex=r.nextInt(numwithletter);

		while(i<=randomAnswerIndex){
			if(inPlay[j]==false){//remember when inPlay[j] is false it means dictionary[j] is in play.
				i++;
			}
			j++;
		}
		return dictionary[j-1];//in the former code, j is added by 1 even though i is reached the random index, so we reduce j by 1 when picking the answer word.
	}

	//Randomly generates a temporary answer among the words in play that don't contain the wrong letter.
	//The idea if this method is similar to assumeWordCorrect().
	public static String assumeWordWrong(int netSize, int numwithletter){
		Random r=new Random();//create a random object for generating temporary answer.
		int i=0;//ie: i=4 means in the dictionary array,among the words that in play, the forth word that doesn't contain that wrong letter is the temporary answer.
		int j=0;
		int randomAnswerIndex=r.nextInt(netSize-numwithletter);//(netSize-numwithletter) is the number of words still in play.
		while(i<=randomAnswerIndex){
			if(inPlay[j]==false){
				i++;
			}
			j++;
		}
		return dictionary[j-1];//same idea.
	}

	/*Note: there may be a more efficient algorithm.
	 * this method is called only when a letter is determined to not be in the answer.
	 * If mark[i] is true, it means dictionary[i] must contains a letter that is not in the answer, it may contains a letter that is in the answer.
	 * But since it contains a letter that is not in the answer, the word is not in play anymore.
	 * If mark[i] is false, it means dictionary[i] doesn't contain wrong letters. It either contains right letters,
	 *  or is made up by the letters the player hasn't guessed (unknown letters).
	 */
	public static void mark(){//for the word contain guessed but wrong letter(inplay true), mark in the third array. mark from false to true
		for(int i=0;i<DICTIONARYSIZE;i++){
			if(inPlay[i]==true)
				mark[i]=true;
		}
	}

	/*Note: there may be a more efficient algorithm.
	 * this method is called only when a letter is determined to be in the answer.
	 * Please check the comment for the mark method to learn about what mark[i] is false means.
	 * Now that a right letter is guessed and revealed, the words that are made up by unknown letters(type A) are no longer in play, 
	 * only the words that contain right letters instead of wrong letters (type B) are in play.
	 * Unfortunately, currently type B are not in play(their inPlay[i] are true), type A is in play (their inPlay[i] are false).
	 * we need to change the inPlay of type A and B to their opposite.
	 * But not only type B's inPlay[i] are true, remember the words contain wrong letters(type C), their inPlay[i] are also true.
	 * 
	 * That's why we create a mark array, it marks type C's mark[i] as true, marks type A and B's mark[i] as false.
	 * So, to change type A's inPlay[i] from false to true(makes them no longer in play) and change type B's inPlay[i] from true to false(makes them in play),
	 * we change the inPlay[i] of the words,which mark[i] is false, to their opposite.
	 */
	public static void cleanup(){
		for(int i=0;i<DICTIONARYSIZE;i++){
			if(mark[i]==false ){
				inPlay[i]=!inPlay[i];
			}
		}
	}

	//updates the incorrect letters record and number of question marks by changing the question mark at a certain spot into the wrong letter.
	public static void incorrectSoFar(int haveGuessed,char guessLetter){
		lettersAndMarks[haveGuessed-1]=guessLetter;
	}

	//prints out the lettersAndMarks array.
	public static void printLettersAndMarks(int guessTimeTotal){
		for(int i=0;i<guessTimeTotal;i++){
			System.out.print(lettersAndMarks[i]);
		}
		System.out.println();
	}

	//prints out the hangman grid.
	public static void printGrid(){
		for(int n=0;n<=grid.length-1;n++){
			System.out.print(grid[n]);
		}
		System.out.println();
	}

	//reveals the right letter in the right spot within the temporary answer when the player guessed out the right letter.
	public static void locateRightLetter(String assumeAnswer,char guessLetter){
		for(int i=0;i<=assumeAnswer.length()-1;i++){//go through the whole word
			if(assumeAnswer.charAt(i)==guessLetter){
				grid[i]=guessLetter;
			}
		}
	}

	//this method returns the number of words that were in play but will not be in play anymore: the words contains the right letter but the right letter is not in the right place.
	public static int narrow(char guessLetter){
		int test=0;
		for(int j=0;j<DICTIONARYSIZE;j++){ 
			if(inPlay[j]==false){int n=0;//go through every word who were in play.
			for(int i=0;i<WORDLENGTH;i++){//examines every spot in the word.
				//if the right letter is not in that spot in the answer but it is in the same spot in a word in play, take that word from the in play list.
				if(grid[i]=='?'){
					if(dictionary[j].charAt(i)==guessLetter){
						//both mark array and inPlay array need to be changed.
						inPlay[j]=true;
						mark[j]=true;
						n++;
					}
				}
				//if the right letter is in that spot in the answer but is is not in the same spot in a word in play,take that word from the in play list.
				if(grid[i]==guessLetter){
					if(dictionary[j].charAt(i)!=guessLetter){
						//both mark array and inPlay array need to be changed.
						inPlay[j]=true;
						mark[j]=true;
						n++;
					}
				} 
			}
			//if a word meets at least one criteria, it need to be taken out.
			if(n>0)
				test++;
			} 
		}
		return test;
	}

	//gives feedback when the player wins or loses, reveals the answer.
	public static void feedback(int haveGuessed,boolean win,String assumeAnswer){
		if(!win)
			System.out.println("You lose. The word was: "+assumeAnswer);
		else{
			System.out.println("CONGRATULATIONS! You win! The word was: "+assumeAnswer);
			//for the player who wins, tells the player how many times they guessed wrong.
			System.out.println("You beat the computer! It takes you "+haveGuessed+" times.");
		}
	}
	//tests if the player wins after examining a guessed letter.
	public static boolean testIfWin(){
		//calculates the amount of question marks that are left in the grid
		int numMark=0;
		for(int i=0;i<WORDLENGTH;i++){
			if(grid[i]=='?')
				numMark++;
		}
		//if no question mark is left, which means all the letters in the answer were revealed, the player wins.
		if(numMark==0){
			return true;
		}
		else
			return false;
	}

	//give appropriate message when the player refuse to start a new game.
	public static void replyNo(boolean win){
		//if the player wins, or the player has never start a game, say "see you next time".
		if(win==true)
			System.out.println("See you next time.");
		//if the player lost, wish him/her a better luck
		//It is inappropriate to say better luck next time if the player never started a game.
		else
			System.out.println("Better luck next time.");
		
	}
}