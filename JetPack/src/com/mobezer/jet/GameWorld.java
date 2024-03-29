package com.mobezer.jet;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mobezer.jet.objects.Bob;
import com.mobezer.jet.objects.Coin;
import com.mobezer.jet.objects.Enemey;

public class GameWorld {
	// World constants
	public static final int WORLD_STATE_RUNNING = 0;
	public static final int WORLD_STATE_NEXT_LEVEL = 1;
	public static final int WORLD_STATE_GAME_OVER = 2;
	public static final int GAME_UNIT = 48;
	public static final int WORLD_WIDTH = GlobalSettings.VIRTUAL_WIDTH;
	public static final int WORLD_HEIGHT = GlobalSettings.VIRTUAL_HEIGHT;
	public static final Vector2 gravity = new Vector2(0, -100);
	public static OrthographicCamera camera; // camera to obtain projection
	public static int touchstate=0;
	// particles
	ParticleEffectPool smokeEffectPool;
	Array<PooledEffect> effects = new Array<PooledEffect>();
									
	// Others
	public static final Random random=new Random();
	public int state;

	// Lists
	public ArrayList<Enemey> enemies;
	public ArrayList<Coin> coins;
	//public static ArrayList<Package> packages;
	// Game Charaters and core objects
	public Bob bob;
	public float leveledSoFar = 0;
	public float heightSoFar;

	// a list of points that define path of the heroBall
	float stateTime,scoreTime=0,delta = 1f;
	private TextureWrapper backTexture;
	private ShapeRenderer shapes = new ShapeRenderer();
	public GameWorld(OrthographicCamera cam) {
		GameWorld.camera = cam;
		bob = new Bob(180, 80);
		enemies = new ArrayList<Enemey>();
		coins = new ArrayList<Coin>();
		leveledSoFar = 400;
		backTexture = new TextureWrapper(Assets.backgroundRegion, new Vector2(
				GlobalSettings.VIRTUAL_WIDTH / 2,
				GlobalSettings.VIRTUAL_HEIGHT / 2));
		backTexture.SetDimension(GlobalSettings.VIRTUAL_WIDTH, GlobalSettings.VIRTUAL_HEIGHT);
		ParticleEffect smokeEffect = new ParticleEffect();
		smokeEffect.load(Gdx.files.internal("particles/smoke.p"), Assets.getAtlas("game"));
		smokeEffectPool = new ParticleEffectPool(smokeEffect, 1, 1);
		PooledEffect effect = smokeEffectPool.obtain();
		effect.setPosition(bob.position.x-50, bob.position.y-50);
		effects.add(effect);
		this.state = WORLD_STATE_RUNNING;
	}

	public void update(float delta) {
		if (state == WORLD_STATE_RUNNING) {		
			delta*=this.delta;
			stateTime+=delta;
			updateLevel(delta);
			updateBob(delta);
			updateEnemy(delta);
			updateCoins(delta);
			updateClouds(delta);
			heightSoFar = Math.max(bob.position.y, heightSoFar);
			if (bob.state != Bob.BOB_STATE_HIT)
				checkCollisions();
			checkGameOver();
		}
	}


	private void updateLevel(float delta){
		if(bob.position.y+600<leveledSoFar)
			return;
		float y = leveledSoFar+20;
		int right = 0,left=0;
		float diff=200;
		float x;
		while (y < leveledSoFar + WORLD_WIDTH * 2) {
			float off=20;
			if(random.nextBoolean()==true){
				off=-20+WORLD_WIDTH/2;
			}
			x = off+random.nextFloat()* (WORLD_WIDTH/2 - Enemey.ENEMEY_WIDTH)
					+ Enemey.ENEMEY_WIDTH / 2;
			if(x>WORLD_WIDTH/2){ 
				left = 0;
				right++;
			}
			else{
				right = 0;
				left++;
			}
			if(right>=2){
				right = 0;
				off = 20;
				x = off+random.nextFloat()* (WORLD_WIDTH/2 - Enemey.ENEMEY_WIDTH)
						+ Enemey.ENEMEY_WIDTH / 2;
			}
			if(left>=2){
				left = 0;
				off=-20+WORLD_WIDTH/2;
				x = off+random.nextFloat()* (WORLD_WIDTH/2 - Enemey.ENEMEY_WIDTH)
						+ Enemey.ENEMEY_WIDTH / 2;
			}
			Enemey ene = new Enemey(x, y);
			enemies.add(ene);
			//addCoins(ene);
			/*oneItem = false;
			platforms.add(platform);
			createMashrooms(platform);
			createSheilds(platform);
			createWings(platform);
			createDiamonds(platform);
			createEnemies(platform);*/
	
			y += (diff / 1.4f);
			y -= random.nextFloat() * 20;
		}
		leveledSoFar = y;
	}
	@SuppressWarnings("unused")
	private void addCoins(Enemey ene) {
		float ran = random.nextFloat();
		if(ran>0.75){
			int off = (random.nextBoolean()==true)?1:-1;
			off*=40;
			float x = random.nextFloat()*(WORLD_WIDTH-60);
			float y = ene.position.y+off;
			Coin coin = new Coin(x, y);
			coins.add(coin);
		}
		if(ran >= 0.6 && ran<=0.85){
			int size = enemies.size();
			boolean occuppied = false;
			/*for(int i = 0;i<size;i++){
				if(enemies.get(i).position.y<ene.position.y){
					if(enemies.get(i).position.y<ene.position.y-120&&enemies.get(i).half==ene.half){
						occuppied = true;
					}
				}
			}*/
			/*if(occuppied == false){
				//float y = ene.position.y-30;
				for(float y= (ene.position.y-30);y>ene.position.y-140;y-=20){
					float x = ene.position.x;
					Coin coin = new Coin(x, y);
					coins.add(coin);
				}
			}*/
		}
		/*else if(ran>=0.85){
			for(int i=0;i<360;i+=36){
				float angle = i*MathUtils.degreesToRadians;
				float x = ene.position.x+((Enemey.ENEMEY_WIDTH/2+15)*MathUtils.cos(angle));
				float y = ene.position.y+((Enemey.ENEMEY_HEIGHT/2+10)*MathUtils.sin(angle));
				Coin coin = new Coin(x, y);
				coins.add(coin);
			}
		}*/
	}

	private void updateBob(float delta) {
		if(bob.state==Bob.BOB_STATE_FLY)
			camera.position.y = bob.position.y + 160f;
		camera.update();
		if(scoreTime>4){
			Bob.SCORE = (int) heightSoFar/32;
			scoreTime = 0;
		}
		scoreTime++;
		bob.Update(delta);
	}

	private void updateEnemy(float delta) {
		int size = enemies.size();
		for(int i = 0;i<size;i++){
			Enemey item = enemies.get(i);
			if(item.position.y<camera.position.y-500){
				enemies.remove(i);
				size = enemies.size();
				continue;
			}
			item.Update(delta);
		}
		System.out.println("Clouds = "+size);
	}
	private void updateCoins(float delta) {
		int size = coins.size();
		for(int i = 0;i<size;i++){
			Coin item = coins.get(i);
			if(item.position.y<camera.position.y-500){
				coins.remove(i);
				size = coins.size();
				continue;
			}
			item.Update(delta);
		}
	}
	private void updateClouds(float delta) {
		// TODO Auto-generated method stub
		
	}

	public static OrthographicCamera getCamera() {
		return camera;
	}

	public void render(SpriteBatch batch) {
		batch.end();
		//2. clear our depth buffer with 1.0
		Gdx.gl.glClearDepthf(1f);
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
		
		//3. set the function to LESS
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		
		//4. enable depth writing
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		
		//5. Enable depth writing, disable RGBA color writing 
		Gdx.gl.glDepthMask(true);
		Gdx.gl.glColorMask(false, false, false, false);
		shapes .setProjectionMatrix(camera.combined);
		shapes.begin(ShapeType.Filled);	 
		shapes.setColor(0f, 1f, 0f, 0.5f);
		shapes.rect(camera.position.x-(GlobalSettings.VIRTUAL_WIDTH/2), camera.position.y-(GlobalSettings.VIRTUAL_HEIGHT/2), GlobalSettings.VIRTUAL_WIDTH, GlobalSettings.VIRTUAL_HEIGHT);	
		shapes.end();
		///////////// Draw sprite(s) to be masked
		batch.begin();
		//8. Enable RGBA color writing
		//   (SpriteBatch.begin() will disable depth mask)
		Gdx.gl.glColorMask(true, true, true, true);
		
		//9. Make sure testing is enabled.
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		
		//10. Now depth discards pixels outside our masked shapes
		Gdx.gl.glDepthFunc(GL20.GL_EQUAL);
		backTexture.setPosition(camera.position.x,camera.position.y);
		batch.disableBlending();
		backTexture.Draw(batch);

		
		
		batch.enableBlending();
		//batch.setColor(Color.GRAY);
		// Update and draw effects:
		for (int i = effects.size - 1; i >= 0; i--) {
		    PooledEffect effect = effects.get(i);
			effect.setPosition(bob.position.x, bob.position.y);
		    effect.draw(batch, Gdx.graphics.getDeltaTime()/2);
		    if (effect.isComplete()) {
		        effect.free();
		        effects.removeIndex(i);
		    }
		}
		//batch.setColor(Color.WHITE);
		bob.Draw(batch);
		renderCoins(batch);
		renderEnemy(batch);
		renderClouds(batch);
		//drawDebug(batch);
	}

	
	@SuppressWarnings("unused")
	private void drawDebug(SpriteBatch batch) {
		batch.end();
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer .setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.polygon(bob.polyBounds.getTransformedVertices());
		int size = enemies.size();
		if(size>0)
			for(int i = 0;i<size;i++){
				Enemey enemey = enemies.get(i);
				shapeRenderer.polygon(enemey.polyBounds.getTransformedVertices());
			}
		size = coins.size();
		if(size>0)
			for(int i = 0;i<size;i++){
				Coin enemey = coins.get(i);
				shapeRenderer.polygon(enemey.polyBounds.getTransformedVertices());
			}
		shapeRenderer.end();
		batch.begin();
	}

	private void renderClouds(SpriteBatch batch) {

	}

	private void renderEnemy(SpriteBatch batch) {
		int size = enemies.size();
		if(size>0)
			for(int i = 0;i<size;i++){
				enemies.get(i).Draw(batch);
			}
	}
	private void renderCoins(SpriteBatch batch) {
		int size = coins.size();
		if(size>0)
			for(int i = 0;i<size;i++){
				coins.get(i).Draw(batch);
			}
	}

	public void dispose() {
		for (int i = effects.size - 1; i >= 0; i--)
		    effects.get(i).free();
		effects.clear();
	}

	public void tap() {
		//bird.fly();
	}

	public void bobMove(float delta, float accel) {	
		if (bob.state == Bob.BOB_STATE_FLY){
			ApplicationType appType = Gdx.app.getType();
			// should work also with
			// Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer)
			if (appType == ApplicationType.Android
					|| appType == ApplicationType.iOS) {
				bob.velocity.x = -accel / 2 * Bob.BOB_MOVE_VELOCITY;
			} else {
				bob.velocity.x = -accel / 10 * Bob.BOB_MOVE_VELOCITY;
			}
		}
			

	}
	private void checkCollisions() {
		checkEnemeyCollisions();
		checkCoinCollisions();
	}

	private void checkEnemeyCollisions() {
		int size = enemies.size();
		if(size>0)
			for(int i = 0;i<size;i++){
				Enemey enemey = enemies.get(i);
				if (Intersector.overlapConvexPolygons(bob.polyBounds, enemey.polyBounds)) {
					bob.hitStorm();					
					WorldListner.hit();
				}
			}
	}
	private void checkCoinCollisions() {
		int size = coins.size();
		if(size>0)
			for(int i = 0;i<size;i++){
				Coin coin = coins.get(i);
				if (Intersector.overlapConvexPolygons(bob.polyBounds, coin.polyBounds)) {
					bob.hitCoin();
					coins.remove(i);
					size = coins.size();
					WorldListner.coin();
				}
			}
	}
	private void checkGameOver() {
		if(bob.state==Bob.BOB_STATE_HIT&&bob.position.y<camera.position.y-240){
			state = WORLD_STATE_GAME_OVER;
		}
	}

	public void touchDown() {
		if(bob.state!=Bob.BOB_STATE_HIT){
			Gdx.app.log("Touch"," accelerate");
			GameWorld.touchstate = 1;
		}
	}

	public void touchUp() {
		if(bob.state!=Bob.BOB_STATE_HIT){
			Gdx.app.log("Touch"," decelerate");
			//GameWorld.touchstate = 0;
		//	bob.state = Bob.BOB_STATE_FALL;
		}
	}



}
