package Logic;

import Boxes.Box;
import Boxes.ColorBox;
import Boxes.FlagBox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Flags extends Category{
	String[] countries = {"Canada", "Ethiopia", "UK", "Sweeden", "Iceland"};
	public Flags(){
		qWidth = 120;
		qHeight = 80;
		int[] xcoords = {0, 0, screenWidth-qWidth, screenWidth-qWidth}; 
		int[] ycoords = {0, screenHeight-qHeight, screenHeight-qHeight, 0}; 
		questionTexture = new Texture(Gdx.files.internal("Iceland.png"));
		
		//question
 	    question = new FlagBox(qWidth, qHeight, "Iceland");
 	    question.getRec().x = screenWidth / 2 - qWidth / 2;
 	    question.getRec().y = screenHeight / 2 - qHeight / 2;
 	    
 	    //answers
 	    answers = new Array<Box>();
 	    for(int i = 0; i < 4; i++){
 	    	FlagBox box = new FlagBox(qWidth, qHeight, countries[i]);
 	 	    box.getRec().x = xcoords[i];
 	 	    box.getRec().y = ycoords[i];
 	 	    answers.add(box);
 	 	    answerTextures[i] = new Texture(Gdx.files.internal(countries[i] + ".png"));
 	    }
	}
	
	@Override
	public Box checkIfHitAnswer(){
		for(Box answer : answers){
			FlagBox a = (FlagBox) answer;
			FlagBox q = (FlagBox) question;
			if(a.getRec().overlaps(q.getRec()) && a.getCountry().equals((q.getCountry()))){
				return a;
			}
		}
		return null;
	}
}
