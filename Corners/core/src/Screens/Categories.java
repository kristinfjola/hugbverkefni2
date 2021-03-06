/**
 * @author: Edda Bjork Konradsdottir
 * @date: 	05.02.2015
 * @goal: 	A class for the categories screen (mostly interface)
 */

package screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.corners.game.MainActivity;

public class Categories implements Screen {

	private SpriteBatch batch;
	private MainActivity main;
	private Stage stage;
	private InputProcessor inputProcessor;
	private Table table;
	
	/**
	 * Constructor. Creates the the interface and sets the
	 * private variables
	 * 
	 * @param main - applicable activity
	 */
	public Categories(MainActivity main){
		this.main = main;
		batch = new SpriteBatch();
		stage = new Stage();

		this.main.scrWidth = Gdx.graphics.getWidth();
		this.main.scrHeight = Gdx.graphics.getHeight();
		main.requestService.showFacebook(false);
		
		addBackToProcessor();
		setAllProcessors();
		
		// Create a table that fills the screen. Everything else will go inside this table.
		this.table = new Table();
		table = new Table();
		table.top();
		table.setFillParent(true);
		stage.addActor(table);
		
		setUpInfoBar();
	}

	@Override
	public void show() {
		Drawable btn = new NinePatchDrawable(main.getPatch("buttons/mainButton.9.png",
				Integer.MAX_VALUE,main.scrHeight/8));
		TextButtonStyle style = new TextButtonStyle(btn, btn, btn,
				main.skin.getFont(main.screenSizeGroup+"-L"));
		style.fontColor = Color.BLACK;
		
		TextButton btnMath = makeButton("Math", style);
		TextButton btnColors = makeButton("Colors", style);
		TextButton btnFlags = makeButton("Flags", style);
		
		
		table.add(new Label(main.data.getName(), main.skin, main.screenSizeGroup+"-M"))
				.padTop(main.scrHeight*0.4f-main.scrHeight/10).padBottom(main.scrHeight/40).row();
		table.add(btnMath).size(main.scrWidth/1.5f, main.scrHeight/8).padBottom(main.scrHeight/20).row();
		table.add(btnColors).size(main.scrWidth/1.5f, main.scrHeight/8).padBottom(main.scrHeight/20).row();
		table.add(btnFlags).size(main.scrWidth/1.5f, main.scrHeight/8).padBottom(main.scrHeight/20).row();
	}
	
	/**
	 * Makes a new text button for the start screen
	 * @param text
	 * @param style
	 * @return
	 */
	private TextButton makeButton(final String text, TextButtonStyle style){
		TextButton button = new TextButton(text, style);
		button.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				dispose();
				if(text.equals("Math")){
					main.levels = new Levels(main, new logic.Math());
					main.setScreen(main.levels);
				}else if(text.equals("Colors")){
					main.levels = new Levels(main, new logic.Colors());
		            main.setScreen(main.levels);
				}else if(text.equals("Flags")){
					main.levels = new Levels(main, new logic.Flags());
		            main.setScreen(main.levels);
				}
			}
		});
		return button;
	}
	
	/**
	 * Renders all the cool stuff on the screen every delta time
	 * 
	 * @param delta
	 */
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(21/255f, 149/255f, 136/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.begin();
        batch.draw(main.background, 0, 0, main.scrWidth, main.scrHeight);
		batch.draw(main.character.getCharacterImg(), main.scrWidth*0.25f,
				main.scrHeight*0.6f, main.scrWidth*0.5f, main.scrWidth*0.5f);	
        batch.end();
		
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
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

	/**
	 * Disposes the screen
	 */
	@Override
	public void dispose() {
		stage.dispose();
	}

	/**
	 * Creates a input processor that catches the back key 
	 */
	private void addBackToProcessor() {
		inputProcessor = new InputProcessor() {
			
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				return false;
			}
				
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				return false;
			}
				
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				return false;
			}
				
			@Override
			public boolean scrolled(int amount) {
				return false;
			}
				
			@Override
			public boolean mouseMoved(int screenX, int screenY) {
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
			public boolean keyDown(int keycode) {
				if(keycode == Keys.BACK){
					main.setScreen(new Start(main));
				}
				return false;
			}
		};
	}
	
	/**
	 * Adds the game stage and the back button processors to a multiplexer
	 */
	private void setAllProcessors() {
		Gdx.input.setCatchBackKey(true);
		
		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(inputProcessor);
		Gdx.input.setInputProcessor(multiplexer); 	
	}
	
	/**
	 * Sets up the info bar
	 */
	public void setUpInfoBar() {
		double tempStars = main.data.getAllAverageStars();
		int tempLevels = main.data.getAllFinished();
		InfoBar infoBar = new InfoBar(main);
		infoBar.setMiddleText("Categories");
		infoBar.setRightText(tempLevels+"/27");
		infoBar.setLeftImages(main.getStarImgs(tempStars));
		table.add(infoBar.getInfoBar()).size(main.scrWidth, main.scrHeight/10).fill().row();
	}
}
