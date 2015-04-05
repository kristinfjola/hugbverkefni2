/**
 * @author: Johanna Agnes Magnusdottir
 * @date: 	05.02.2015
 * @goal: 	A class for the Start screen (mostly interface)
 */

package screens;

import logic.Category;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.corners.game.MainActivity;

public class Start implements Screen{
	private MainActivity main;
	private Stage stage;
	private SpriteBatch batch;
	private Skin skin;
	Category cat;
	private Texture carl;
	Table table;
	private InputProcessor inputProcessor;
	
	/**
	 * Constructor. Creates the the interface and sets the
	 * private variables
	 * 
	 * @param main - applicable activity
	 */
	public Start(MainActivity main){

		this.main = main;		
		stage = new Stage();
		batch = new SpriteBatch();
		skin = main.skin;
		this.cat = new Category();
		carl = main.character.getCharacterImg();
		Gdx.input.setInputProcessor(stage);
		main.activityRequestHandler.showFacebook(false);
		addBackToProcessor();
		setAllProcessors();
	}
	
	/**
	 * Creates and sets up the buttons on the start screen
	 */
	@Override
	public void show() {
		this.table = new Table();
		table.top();
		table.setFillParent(true);
		stage.addActor(table);
				
		setUpInfoBar();
		
		Drawable btn = new NinePatchDrawable(main.getPatch("buttons/mainButton.9.png",Integer.MAX_VALUE,main.scrHeight/8));
		TextButtonStyle style = new TextButtonStyle(btn, btn, btn, main.skin.getFont(main.screenSizeGroup+"-L"));
		style.fontColor = Color.BLACK;

		final TextButton btnCategories = new TextButton("Play",style);
		btnCategories.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				dispose();
				main.categories = new Categories(main);
				main.setScreen(main.categories);
			}
		});
		
		TextButton btnSettings = new TextButton("Settings",style);
		btnSettings.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				dispose();
				main.settings = new Settings(main);
				main.setScreen(main.settings);
			}
		});
		
		TextButton btnFriends = new TextButton("Friends",style);
		btnFriends.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				dispose();
				main.friends = new Friends(main);
				main.setScreen(main.friends);
			}
		});
		
		table.add(new Label(main.data.getName(), main.skin, main.screenSizeGroup+"-M"))
				.padTop(main.scrHeight*0.4f-main.scrHeight/10).padBottom(main.scrHeight/40).row();
		table.add(btnCategories).size(main.scrWidth/1.5f, main.scrHeight/8).padBottom(main.scrHeight/20).row();
		table.add(btnSettings).size(main.scrWidth/1.5f, main.scrHeight/8).padBottom(main.scrHeight/20).row();
		table.add(btnFriends).size(main.scrWidth/1.5f, main.scrHeight/8).padBottom(main.scrHeight/20).row();
		
	}

	/**
	 * Renders stuff on the screen every delta time
	 * 
	 * @param delta
	 */
	@Override
	public void render(float delta) {	
		//Gdx.gl.glClearColor(21/255f, 149/255f, 136/255f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);	
		
		batch.begin();
		batch.draw(main.background, 0, 0, main.scrWidth, main.scrHeight);
		batch.draw(carl, main.scrWidth*0.25f, main.scrHeight*0.6f, main.scrWidth*0.5f, main.scrWidth*0.5f);	
		batch.end();
		
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

	/**
	 * Resizes the screen to the applicable size, the parameters
	 * width and height are the new width and height the screen 
	 * has been resized to
	 * 
	 * @param width
	 * @param height
	 */
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
	 * Sets up the info bar
	 */
	public void setUpInfoBar() {
		InfoBar infoBar = new InfoBar(main);
		infoBar.setMiddleText("Corners");
	 	table.add(infoBar.getInfoBar()).size(main.scrWidth, main.scrHeight/10).fill().row();
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
			
			/**
			 * Handles the back event
			 */
			@Override
			public boolean keyDown(int keycode) {
				if(keycode == Keys.BACK){
					Gdx.app.exit();
					//dispose();
					main.activityRequestHandler.unregisterRingerReceiver();
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
}
