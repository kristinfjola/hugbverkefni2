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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.corners.game.MainActivity;

import data.Data;

public class Play implements Screen, InputProcessor{

	private MainActivity main;
	private Category cat;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Stage stage;
	private State state;
    private int screenWidth = Gdx.graphics.getWidth();
    private int screenHeight = Gdx.graphics.getHeight();
    private int level;
    public Data data;
    
    private int questionsAnswered = 0;
    private int nrOfQuestions;
    private float origX;
    private float origY;
    
    // infoBar
    private InfoBar infoBar;
    private Table table;
    private InputProcessor inputProcessor;

    // swipe
    private Vector3 touchPos;
    private boolean swipeQuestion = false;
    private boolean hitWrong = false;
    private boolean touchUp = false;
    private float sum = 0;
    private String xDirection = "none";
    private String yDirection = "none";
    private boolean lockPos = false;

    // time
    private long startTime = 0;	
    private long secondsPassed;
    private long totalSecondsWasted = 0;
    private long oldSecondsPassed = 0;
    private ProgressBar progressBar;
    private ProgressBarStyle progressBarStyle;
    private BitmapFont time;
    private long maxTime;
    private boolean delayTime = false;
    private int stars = 3;
    
    // levels
    private int maxNumLevels = 9;
    
    // sound
    Sound correctAnswerSound = Gdx.audio.newSound(Gdx.files.internal("sounds/correctAnswer.mp3"));
    Sound wrongAnswerSound = Gdx.audio.newSound(Gdx.files.internal("sounds/wrongAnswer.mp3"));
    Sound levelFinishedSound = Gdx.audio.newSound(Gdx.files.internal("sounds/levelFinished.mp3"));
    Sound categoryFinishedSound = Gdx.audio.newSound(Gdx.files.internal("sounds/levelFinished.mp3"));
	
	/**
	 * @param main - main activity of the game
	 * @param cat - what category is being played
	 */
	public Play(MainActivity main, Category cat, int level){
		this.main = main;
		this.infoBar = new InfoBar(main);
		this.cat = cat;
		this.level = level;
		this.state = State.RUN;
		this.stage = new Stage();
		this.table = new Table();
		this.table.top().setFillParent(true);
		
		addPauseToProcessor();
		setAllProcessors(); 
		this.data = cat.getData();
		
		setUpInfoBar();
	
		origX = screenWidth/2 - cat.getQuestion().getRec().getWidth()/2;
	    origY = screenHeight/2 - cat.getQuestion().getRec().getHeight()/2;
 	    Gdx.input.setCatchBackKey(true);
	    time = main.skin.getFont(main.screenSizeGroup+"-M");
	    time.setColor(Color.BLACK);
	    maxTime = 10;
	    nrOfQuestions = 9;
		camera = new OrthographicCamera();
 	    camera.setToOrtho(false, screenWidth, screenHeight);
 	    batch = new SpriteBatch();
 	    createProgressBar();
 	    startQuestion();
	}
	
	/**
	 * @author eddabjorkkonradsdottir
	 * 
	 * handles the state of the game - pause/run
	 */
	public enum State {
		PAUSE,
		RUN
	}

	/**
	 * Renders a question in the middle of the screen and 4 answers located
	 * at each corner of the screen. Handles when user touches the screen
	 * and drags/swipes the question.
	 */
	@Override
	public void render(float delta) {
		switch (state) {
		case RUN:
			update();
			break;
		case PAUSE:
			break;
		}
		
		draw();
	}
	
	/**
	 * Resets the level and notifies user that he has won
	 */
	public void win(){
		saveStars(stars);
		questionsAnswered = 0;
		totalSecondsWasted = 0;
		setCorrectProgressBar();
		
		if(level < maxNumLevels) { 
			showFinishLevelDialog(true, false);
			level++;
			levelFinishedSound.play(main.volume);
		} else {
			showFinishLevelDialog(true, true);
			categoryFinishedSound.play(main.volume);
		}
	}
	
	/**
	 * Moves question to center
	 */
	public void moveQuestionToStartPos(){
		cat.getQuestion().getRec().x = origX;
		cat.getQuestion().getRec().y = origY;
	}
	
	/**
	 * @param touchPos
	 * @return true if you touch the question
	 */
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
	
	/**
	 * notifies user when he looses a level
	 */
	public void loose(){
		setWrongProgressBar();
		refreshProgressBar(true);
		delayTime = true;
		showFinishLevelDialog(false, false);
		wrongAnswerSound.play(main.volume);
	}
	
	/**
	 * resets time on progress bar
	 */
	public void resetTime(){
		progressBar.setValue(0);
		startTime = System.nanoTime();
	}
	
	/**
	 * sets first question
	 */
	public void startQuestion(){
		moveQuestionToStartPos();
		cat.generateNewQuestion(level);
		resetTime();
	}
	
	/**
	 * sets new question
	 */
	public void getNewQuestion(){
		startQuestion();
		setNormalProgressBar();
		refreshProgressBar(false);	
	}
	
	/**
	 * draws the progressbar
	 */
	public void drawProgressBar(){
		long endTime = System.nanoTime();
		if(!delayTime){
			secondsPassed = oldSecondsPassed + (endTime - startTime)/1000000000;  
		}
		progressBar.setValue(secondsPassed);
		long timeLeft = (maxTime - secondsPassed) >= 0 ? (maxTime - secondsPassed) : 0;
		
		float xCoord = screenWidth/2-(time.getBounds(Long.toString(timeLeft)).width)/2;
		float yCoord = screenHeight-infoBar.barHeight-progressBar.getPrefHeight()*1.5f;
		time.draw(batch, Long.toString(timeLeft), xCoord, yCoord);
	}
	
	/**
	 * notifies user that his answer was correct
	 */
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
		updateStars();
		correctAnswerSound.play(main.volume);
	}
	
	/**
	 * sets up progress bar
	 */
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
 	    progressBar.setPosition(0,screenHeight-screenHeight/10-progressBar.getPrefHeight());
	    progressBar.setSize(screenWidth, progressBar.getPrefHeight());
	    progressBar.setAnimateDuration(1);
	    stage.addActor(progressBar);
	}
	
	/**
	 * Shows a pop-up screen with information whether the player
	 * finished a level or a category, and how many stars earned
	 * in the corresponding level.
	 * 
	 * @param win - true if hit right answer
	 * @param finishCat - true if the level is the last level in the
	 * category (there for the player has finished the category)
	 */
	public void showFinishLevelDialog(boolean win, boolean finishCat) {
		pause();
		
		Dialog dialog = new Dialog("", this.main.skin);
		if(win) dialog.getContentTable().row();
		else dialog.text("Oh no!");
		dialog.getContentTable().row();
		if(win && !finishCat) dialog.text("Level complete!");
		else if(finishCat) dialog.text(cat.getType()+" complete!");
		else dialog.text("You lost!");
		dialog.getContentTable().row();
		
		if(win) {
			Table starsTable = getStars(stars);
			dialog.getContentTable().add(starsTable);
			if(finishCat) {
				dialog.getContentTable().row();
				dialog.text("Try to get more stars in each level!");
			}
		} else {
			Table sadFaceTable = getSadFace();
			dialog.getContentTable().add(sadFaceTable);
		}
		
		dialog.show(this.stage);
		Timer.schedule(new Task(){
		    @Override
		    public void run() {
		        // Do your work
		    	main.setScreen(new Levels(main, cat));
		    }
		}, 3);
		
	}
	
	/**
	 * @param numStars - number of stars earned
	 * @return table of pictures of the stars 
	 * 					(both earned and lost)
	 */
	public Table getStars(int numStars) {
		int maxStars = 3;
		int loseStars = maxStars-numStars;
		
		Table starsTable = new Table();
		
		Texture yellow_star = new Texture("stars/star_yellow.png");
		Texture gray_star = new Texture("stars/star_gray.png");
		
		for(int i = 1; i <=numStars; i++) {
			Image win_star = new Image();
			win_star.setDrawable(new TextureRegionDrawable(new TextureRegion(yellow_star)));
			starsTable.add(win_star).padTop(30).padBottom(30);
		}
		
		for(int i = 1; i <= loseStars; i++) {
			Image lose_star = new Image();
			lose_star.setDrawable(new TextureRegionDrawable(new TextureRegion(gray_star)));
			starsTable.add(lose_star).padTop(30).padBottom(30);
		}
		
		return starsTable;
	}
	
	/**
	 * @return table with a picture of a sad smiley face
	 */
	public Table getSadFace() {
		Table sadFaceTable = new Table();
		
		Texture sad_face = new Texture("faces/sad_face.png");
		
		Image sad = new Image();
		sad.setDrawable(new TextureRegionDrawable(new TextureRegion(sad_face)));
		sadFaceTable.add(sad).padTop(50);
		
		return sadFaceTable;
	}
	
	/**
	 * places question on top of answer
	 */
	public void moveQuestionOverAnswer() {
		if(xDirection == "left" ) {
			cat.getQuestion().getRec().x = 0;
		}
	}
		
	/**
	 * sets new progress bar
	 * @param delay
	 */
	public void refreshProgressBar(boolean delay){
		progressBar.remove();
		progressBar = new ProgressBar(0, maxTime, 0.5f, false, progressBarStyle);
		progressBar.setPosition(0,screenHeight-screenHeight/10-progressBar.getPrefHeight());
	    progressBar.setSize(screenWidth, progressBar.getPrefHeight());
	    progressBar.setAnimateDuration(delay ? 0 : 1);
	    progressBar.setValue(secondsPassed);
	    stage.addActor(progressBar);
	}
	
	/**
	 * sets normal progress bar
	 */
	public void setNormalProgressBar(){
		progressBarStyle.background = main.skin.getDrawable("bg");
		progressBarStyle.knob = main.skin.getDrawable("knob");
		progressBarStyle.knobBefore = progressBarStyle.knob;
	}
	
	/**
	 * sets the progress bar that notifies the user
	 * when he has won
	 */
	public void setCorrectProgressBar(){
		progressBarStyle.background = main.skin.getDrawable("bg_correct");
		progressBarStyle.knob = main.skin.getDrawable("knob_correct");
		progressBarStyle.knobBefore = progressBarStyle.knob;
	}
	
	/**
	 * sets the progress bar that notifies the user
	 * when he has lost
	 */
	public void setWrongProgressBar(){
		progressBarStyle.background = main.skin.getDrawable("bg_wrong");
		progressBarStyle.knob = main.skin.getDrawable("knob_wrong");
		progressBarStyle.knobBefore = progressBarStyle.knob;
	}
	
	/**
	 * calculates stars for finishing the level within the time limit 
	 */
	public void updateStars(){
		totalSecondsWasted += secondsPassed;
		
		int threeStars = 20;
		int twoStars = 45;
		int oneStar = 80;
		
		if(totalSecondsWasted >= threeStars && stars == 3){
			stars = 2;
		} else if(totalSecondsWasted >= twoStars && stars == 2){
			stars = 1;
		} else if(totalSecondsWasted >= oneStar && stars == 1){
			stars = 0;
		}
		
		//update picture in info bar
		table.reset();
		table.top();
		table.setFillParent(true);
		infoBar.setLeftImage(""+stars+"-stars");
		table.add(infoBar.getInfoBar()).size(screenWidth, screenHeight/10).fill().row();
	}
	
	@Override
	public void show() {
	}

	@Override
	public void resize(int width, int height) {
	}

	/**
	 * pauses the game
	 */
	@Override
	public void pause() {
		oldSecondsPassed = secondsPassed;
		delayTime = true;
		this.state = State.PAUSE;
	}

	/**
	 * resumes the game
	 */
	@Override
	public void resume() {
		startTime = System.nanoTime();
		delayTime = false;
		this.state = State.RUN;
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
	
	/**
	 * Checks if the question box has gone out of upper
	 * boundaries of the screen
	 * 
	 * @param axis - y or x axis
	 */
	public void checkUpperBoundaries(String axis) {
		Rectangle rec = cat.getQuestion().getRec();
		
		if(axis == "x") {
			if(rec.x > screenWidth-rec.getWidth()) {
				rec.x = screenWidth-rec.getWidth();
			}
		} else if(axis == "y") {
			//screenHeight/10 is the height of the infoBar
			if(rec.y > screenHeight-rec.getHeight()-screenHeight/10) {
				rec.y = screenHeight-rec.getHeight()-screenHeight/10;
			}
		}	
	}
	
	/**
	 * Checks if the question box has gone out of lower
	 * boundaries of the screen
	 * 
	 * @param axis - y or x axis
	 */
	public void checkLowerBoundaries(String axis) {
		Rectangle rec = cat.getQuestion().getRec();
		
		if(axis == "x") {
			if(rec.x < 0) {
				rec.x = 0;
			}
		} else if(axis == "y") {
			if(rec.y < 0) {
				rec.y = 0;
			}
		}
	}
	
	/**
	 * Swipes the question smoothly after the user has let go
	 * of the question box (after touchUp)
	 */
	public void swipeQuestionAfterTouchUp() {
		Rectangle rec = cat.getQuestion().getRec();
		
		if(touchUp && swipeQuestion && !hitWrong) {
			if(xDirection != "none") {
				if(xDirection == "right") {
					rec.x += 6;
					checkUpperBoundaries("x");
				} else {
					rec.x -= 6;
					checkLowerBoundaries("x");
				}
			}
			if(yDirection != "none") {
				if(yDirection == "up") {
					rec.y += 12;
					checkUpperBoundaries("y");
				} else {
					rec.y -= 12;
					checkLowerBoundaries("y");
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
	}
	
	/**
	 * Sets the direction of the question box
	 * (in which direction it is swiped)
	 */
	public void setDirections() {
		Rectangle rec = cat.getQuestion().getRec();
		if(rec.x - origX < 30 && rec.x - origX > -30) {
			xDirection = "none";
		} else {
			if(rec.x > origX) {
				xDirection = "right";
			} else {
				xDirection = "left";
			}
		}
		if(rec.y - origY < 30 && rec.y - origY > -30) {
			yDirection = "none";
		} else {
			if(rec.y > origY) {
				yDirection = "up";
			} else {
				yDirection = "down";
			}
		}
	}
	
	/**
	 * Handles when the question has hit a answer box,
	 * whether the answer is right or wrong
	 */
	public void handleHitBox() {
		Box hit = cat.checkIfHitAnswer();
		if(hit != null && !delayTime){
			questionsAnswered++;
			if(questionsAnswered >= nrOfQuestions){
				win();
			} else {
				displayRightAnswerAndGetNewQuestion();
			}
		}
		
		//if hit wrong answer then move question to the middle
		Box hitBox = cat.checkIfHitBox();
		if(hitBox != null && hit == null && !delayTime) {
			hitWrong = true;
			loose();
		}
	}
	
	/**
	 * The update loop of the game - runs when the game is in
	 * RUN state
	 */
	public void update() { 
		Rectangle rec = cat.getQuestion().getRec();
		
		// swipe question
		if(Gdx.input.isTouched()) {
			touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			if(lockPos || touchOverlapsQuestion(touchPos)){
				if(lockPos) {
					camera.unproject(touchPos);
				}
				
				setDirections();
				
				rec.x = touchPos.x - rec.getWidth() / 2;
				rec.y = touchPos.y - rec.getHeight() / 2;
				lockPos = true;
				swipeQuestion = true;
				hitWrong = false;
			}
		}
		
		// swipe question smoothly after touchUp
		swipeQuestionAfterTouchUp();
		
		// hit answer
		handleHitBox();
		
		// check time
		if(secondsPassed > progressBar.getMaxValue()){
			loose();
		}
	}
	
	/**
	 * Draws everything on the screen
	 */
	public void draw() {
		Gdx.gl.glClearColor(21/255f, 149/255f, 136/255f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
				
		// draw question and answers
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for(Box answer : cat.getAnswers()){
			answer.draw(batch);
		}
		cat.getQuestion().draw(batch);	
		drawProgressBar();
		batch.end();
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}
	
	/**
	 * Creates new processor that handles the pause button
	 */
	private void addPauseToProcessor() {
		 inputProcessor = new InputProcessor() {	
			 @Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
			
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				return false;
			}
			
			/**
			 * Sets a listener to the pause/play button
			 */
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				if(screenX>screenWidth-screenHeight/10 && screenY<screenHeight/10) {
					table.reset();
					table.top();
					table.setFillParent(true);
					if(state == State.RUN) {
						pause();
						infoBar.setRightImage("play");
					}
					else {
						resume();
						infoBar.setRightImage("pause");
					}
					table.add(infoBar.getInfoBar()).size(screenWidth, screenHeight/10).fill().row();
				}
				return false;
			}
			
			@Override
			public boolean scrolled(int amount) { return false; }
			
			@Override
			public boolean mouseMoved(int screenX, int screenY) { return false; }
			
			@Override
			public boolean keyUp(int keycode) { return false; }
			
			@Override
			public boolean keyTyped(char character) { return false; }
			
			@Override
			public boolean keyDown(int keycode) { return false; }
		};
	}
	
	/**
	 * Adds all the processors of the class to a multiplexer
	 */
	private void setAllProcessors() {
		Gdx.input.setCatchBackKey(true);
		
		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(this);
		multiplexer.addProcessor(inputProcessor);
		Gdx.input.setInputProcessor(multiplexer); 	
	}
	
	/**
	 * Sets up the info bar
	 */
	public void setUpInfoBar() {
		stage.addActor(table);
		infoBar.setMiddleText("Level "+level);
		infoBar.setRightText("");
		infoBar.setLeftImage("3-stars");
		infoBar.setRightImage("pause");
	 	table.add(infoBar.getInfoBar()).size(screenWidth, screenHeight/10).fill().row();

	}
	
	public void saveStars(int newStars){
		int levelWon = level;
		openNextLevel(levelWon);
		//cat.updateStars(levelWon, newStars);
		data.getStarsByString(cat.getType()).updateStars(levelWon, newStars);
		cat.saveData(data);
	}

	private void openNextLevel(int levelWon) {
		if(levelWon == 9)
			return;
		int nextLevelStars = data.getStarsByString(cat.getType()).getStarsOfALevel(levelWon + 1);
		if(nextLevelStars > -1)
			return;
		//cat.updateStars(levelWon + 1, 0);
		data.getStarsByString(cat.getType()).updateStars(levelWon + 1, 0);
	}
}