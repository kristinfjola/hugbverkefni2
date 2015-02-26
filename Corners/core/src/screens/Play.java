/**
 * @author: Kristin Fjola Tomasdottir
 * @date	05.02.2015
 * @goal: 	The display of each level which includes a question in the middle
 * 			of the screen and 4 answers located at each corner of the screen
 */
package screens;

import logic.Category;
import boxes.Box;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.corners.game.MainActivity;

public class Play implements Screen, InputProcessor{

	MainActivity main;
	Category cat;
	SpriteBatch batch;
	OrthographicCamera camera;
	private Stage stage;
    boolean hit = false;
    int screenWidth = Gdx.graphics.getWidth();
    int screenHeight = Gdx.graphics.getHeight();
    int level;
    int questionsAnswered = 0;
    int nrOfQuestions;
    float origX;
    float origY;
    
    // swipe
    Vector3 touchPos;
    boolean swipeQuestion = false;
    boolean hitWrong = false;
    boolean touchUp = false;
    float sum = 0;
    String xDirection = "none";
    String yDirection = "none";
    boolean lockPos = false;

    // time
    long startTime = 0;	
    long secondsPassed;
    ProgressBar progressBar;
    ProgressBarStyle progressBarStyle;
    BitmapFont time;
    long maxTime;
    boolean delayTime = false;
    
	
	/**
	 * @param main - main activity of the game
	 * @param cat - what category is being played
	 */
	public Play(MainActivity main, Category cat, int level){
		this.main = main;
		this.cat = cat;
		this.level = level;
		stage = new Stage();
		Gdx.input.setInputProcessor(this);
		origX = screenWidth/2 - cat.getQuestion().getRec().getWidth()/2;
	    origY = screenHeight/2 - cat.getQuestion().getRec().getHeight()/2;
 	    Gdx.input.setCatchBackKey(true);
	    time = cat.getBmFont();
	    time.setColor(Color.BLACK);
	    maxTime = 10;
	    nrOfQuestions = 7;
		camera = new OrthographicCamera();
 	    camera.setToOrtho(false, screenWidth, screenHeight);
 	    batch = new SpriteBatch();
 	    createProgressBar();
 	    startQuestion();
	}

	/**
	 * Renders a question in the middle of the screen and 4 answers located
	 * at each corner of the screen. Handles when user touches the screen
	 * and drags/swipes the question.
	 */
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(21/255f, 149/255f, 136/255f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		
		// draw question and answers
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for(Box answer : cat.getAnswers()){
			//batch.setColor(Color.RED)
			answer.draw(batch);
			//batch.setColor(Color.WHITE);
		}
		cat.getQuestion().draw(batch);	
		drawProgressBar();
		batch.end();
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
		
		// swipe question
		if(Gdx.input.isTouched()) {
			touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			if(lockPos || touchOverlapsQuestion(touchPos)){
				if(lockPos) {
					camera.unproject(touchPos);
				}
				
				if(cat.getQuestion().getRec().x - origX < 30 && 
						cat.getQuestion().getRec().x - origX > -30) {
					xDirection = "none";
				} else {
					if(cat.getQuestion().getRec().x > origX) {
						xDirection = "right";
					} else {
						xDirection = "left";
					}
				}
				if(cat.getQuestion().getRec().y - origY < 30 &&
						cat.getQuestion().getRec().y - origY > -30) {
					yDirection = "none";
				} else {
					if(cat.getQuestion().getRec().y > origY) {
						yDirection = "up";
					} else {
						yDirection = "down";
					}
				}
				
				cat.getQuestion().getRec().x = touchPos.x - cat.getQuestion().getRec().getWidth() / 2;
				cat.getQuestion().getRec().y = touchPos.y - cat.getQuestion().getRec().getHeight() / 2;
				lockPos = true;
				swipeQuestion = true;
				hitWrong = false;
			}
		}
		
		// swipe question smoothly after touchUp
		if(touchUp && swipeQuestion && !hitWrong) {
			if(xDirection != "none") {
				if(xDirection == "right") {
					cat.getQuestion().getRec().x += 6;
					if(cat.getQuestion().getRec().x > screenWidth-cat.getQuestion().getRec().getWidth()) {
						cat.getQuestion().getRec().x = screenWidth-cat.getQuestion().getRec().getWidth();
					}
				} else {
					cat.getQuestion().getRec().x -= 6;
					if(cat.getQuestion().getRec().x < 0) {
						cat.getQuestion().getRec().x = 0;
					}
				}
			}
			if(yDirection != "none") {
				if(yDirection == "up") {
					cat.getQuestion().getRec().y += 12;
					if(cat.getQuestion().getRec().y > screenHeight-cat.getQuestion().getRec().getHeight()) {
						cat.getQuestion().getRec().y = screenHeight-cat.getQuestion().getRec().getHeight();
					}
				} else {
					cat.getQuestion().getRec().y -= 12;
					if(cat.getQuestion().getRec().y < 0) {
						cat.getQuestion().getRec().y = 0;
					}
				}
			}
			
			sum += 3;
			if(sum > 50) {
				touchUp = false;
				swipeQuestion = false;
				sum = 0;
				if (!delayTime){
					moveQuestionToStartPos();
				}
			}
		}
		
		// hit answer
		Box hit = cat.checkIfHitAnswer();
		if(hit != null && !delayTime){
			questionsAnswered++;
			if(questionsAnswered >= nrOfQuestions){
				questionsAnswered = 0;
				level++;
			}
			displayRightAnswerAndGetNewQuestion();
		}
		
		//if hit wrong answer then move question to the middle
		//TODO: we need to make our own overlaps function so this looks good:
		Box hitBox = cat.checkIfHitBox();
		if(hitBox != null && hit == null && !delayTime) {
			hitWrong = true;
			loose();
		}
		
		// check time
		if(secondsPassed > progressBar.getMaxValue()){
			loose();
		}
	}
	
	public void moveQuestionToStartPos(){
		cat.getQuestion().getRec().x = origX;
		cat.getQuestion().getRec().y = origY;
	}
	
	public boolean touchOverlapsQuestion(Vector3 touchPos){
		camera.unproject(touchPos);
		if(touchPos.x >= cat.getQuestion().getRec().x 
				&& touchPos.x <= cat.getQuestion().getRec().x + cat.getQuestion().getRec().getWidth()
				&& touchPos.y >= cat.getQuestion().getRec().y
				&& touchPos.y <= cat.getQuestion().getRec().y + cat.getQuestion().getRec().getHeight()){
			return true;
		}
		return false;
	}
	
	public void loose(){
		setWrongProgressBar();
		refreshProgressBar(true);	
		delayTime = true;
		Timer.schedule(new Task(){
		    @Override
		    public void run() {
		    	main.setScreen(new Levels(main, cat));
		    }
		}, 1);
	}
	
	public void resetTime(){
		progressBar.setValue(0);
		startTime = System.nanoTime();
	}
	
	public void startQuestion(){
		moveQuestionToStartPos();
		cat.generateNewQuestion(level);
		resetTime();
	}
	
	public void getNewQuestion(){
		startQuestion();
		setNormalProgressBar();
		refreshProgressBar(false);	
	}
	
	public void drawProgressBar(){
		long endTime = System.nanoTime();
		if(!delayTime){
			secondsPassed = (endTime - startTime)/1000000000;  
		}
		progressBar.setValue(secondsPassed);
		long timeLeft = (maxTime - secondsPassed) >= 0 ? (maxTime - secondsPassed) : 0;
		time.draw(batch, Long.toString(timeLeft), progressBar.getWidth(), screenHeight/5 - screenHeight/100);
	}
	
	public void displayRightAnswerAndGetNewQuestion(){
		setCorrectProgressBar();
		refreshProgressBar(true);	
		delayTime = true;
		Timer.schedule(new Task(){
		    @Override
		    public void run() {
		    	getNewQuestion();
		    	delayTime = false;
		    }
		}, 1);
	}
	
	public void createProgressBar(){
		main.skin.add("bg", new Texture(Gdx.files.internal("progressBar/background.png")));
		main.skin.add("bg_wrong", new Texture(Gdx.files.internal("progressBar/background_wrong.png")));
		main.skin.add("bg_correct", new Texture(Gdx.files.internal("progressBar/background_correct.png")));
 	    main.skin.add("knob", new Texture(Gdx.files.internal("progressBar/knob.png")));
 	    main.skin.add("knob_wrong", new Texture(Gdx.files.internal("progressBar/knob_wrong.png")));
 	    main.skin.add("knob_correct", new Texture(Gdx.files.internal("progressBar/knob_correct.png")));
 	    progressBarStyle = new ProgressBarStyle(main.skin.getDrawable("bg"), main.skin.getDrawable("knob"));
 	    progressBarStyle.knobBefore = progressBarStyle.knob;
 	    progressBar = new ProgressBar(0, maxTime, 0.5f, false, progressBarStyle);
	    progressBar.setPosition(screenWidth/4, screenHeight/5);
	    progressBar.setSize(screenWidth/2, progressBar.getPrefHeight());
	    progressBar.setAnimateDuration(1);
	    stage.addActor(progressBar);
	}
	
	public void refreshProgressBar(boolean delay){
		progressBar.remove();
		progressBar = new ProgressBar(0, maxTime, 0.5f, false, progressBarStyle);
	    progressBar.setPosition(screenWidth/4, screenHeight/5);
	    progressBar.setSize(screenWidth/2, progressBar.getPrefHeight());
	    progressBar.setAnimateDuration(delay ? 0 : 1);
	    progressBar.setValue(secondsPassed);
	    stage.addActor(progressBar);
	}
	
	public void setNormalProgressBar(){
		progressBarStyle.background = main.skin.getDrawable("bg");
		progressBarStyle.knob = main.skin.getDrawable("knob");
		progressBarStyle.knobBefore = progressBarStyle.knob;
	}
	
	public void setCorrectProgressBar(){
		progressBarStyle.background = main.skin.getDrawable("bg_correct");
		progressBarStyle.knob = main.skin.getDrawable("knob_correct");
		progressBarStyle.knobBefore = progressBarStyle.knob;
	}
	
	public void setWrongProgressBar(){
		progressBarStyle.background = main.skin.getDrawable("bg_wrong");
		progressBarStyle.knob = main.skin.getDrawable("knob_wrong");
		progressBarStyle.knobBefore = progressBarStyle.knob;
	}
	
	@Override
	public void show() {
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void hide() {
		
	}

	@Override
	public void dispose() {
		batch.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Keys.BACK){
			main.setScreen(new Levels(main, cat));
        }
        return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	/**
	 * If user let's go of the question box it returns to the middle
	 * of the screen.
	 */
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		lockPos = false;
		touchUp = true;
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}
