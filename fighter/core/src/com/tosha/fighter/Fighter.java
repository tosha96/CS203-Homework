package com.tosha.fighter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Fighter extends ApplicationAdapter {

    SpriteBatch batch;
    Texture img;
    Box2DDebugRenderer debugRenderer;
    World world;
    OrthographicCamera camera;
    Body groundBody;
    Player player;

    @Override
    public void create() {
        //batch = new SpriteBatch();
        //img = new Texture("badlogic.jpg");
        Box2D.init();
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        
        player = new Player(world);
        
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(new Vector2(0, 10));

        groundBody = world.createBody(groundBodyDef);

        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(camera.viewportWidth, 10.0f);
        groundBody.createFixture(groundBox, 0.0f);
        groundBox.dispose();
    }

    @Override
    public void render() {
        Vector2 vel = player.getBody().getLinearVelocity();
        Vector2 pos = player.getBody().getPosition();
        float ang = player.getBody().getAngularVelocity();

        //WASD to move
        if (Gdx.input.isKeyPressed(Keys.A) && vel.x > -player.getMaxVelocityX()) {
            this.player.getBody().applyLinearImpulse(-player.getSpeedX(), 0, pos.x, pos.y, true);
        }

        if (Gdx.input.isKeyPressed(Keys.D) && vel.x < player.getMaxVelocityX()) {
            this.player.getBody().applyLinearImpulse(player.getSpeedX(), 0, pos.x, pos.y, true);
        }
        
        if (Gdx.input.isKeyPressed(Keys.S) && vel.y > -player.getMaxVelocityY()) {
            this.player.getBody().applyLinearImpulse(0, -player.getSpeedY(), pos.x, pos.y, true);
        }

        if (Gdx.input.isKeyPressed(Keys.W) && vel.y < player.getMaxVelocityY()) {
            this.player.getBody().applyLinearImpulse(0, player.getSpeedY(), pos.x, pos.y, true);
        }
        
        if (Gdx.input.isKeyPressed(Keys.Q) && ang < player.getMaxVelocityAngular()) {
            this.player.getBody().applyAngularImpulse(player.getSpeedAngular(), true);
        }
        
        if (Gdx.input.isKeyPressed(Keys.E) && ang > -player.getMaxVelocityAngular()) {
            this.player.getBody().applyAngularImpulse(-player.getSpeedAngular(), true);
        }
        
        //X breaks
        if (Gdx.input.isKeyPressed(Keys.X)) {
            if (vel.y > 0) this.player.getBody().applyLinearImpulse(0, -player.getBreakSpeedX(), pos.x, pos.y, true);
            else if (vel.y < 0) this.player.getBody().applyLinearImpulse(0, player.getBreakSpeedX(), pos.x, pos.y, true);
            
            if (vel.x > 0) this.player.getBody().applyLinearImpulse(-player.getBreakSpeedY(), 0, pos.x, pos.y, true);
            else if (vel.x < 0) this.player.getBody().applyLinearImpulse(player.getBreakSpeedY(), 0, pos.x, pos.y, true);
            
            if (ang > 0) this.player.getBody().applyAngularImpulse(-player.getBreakSpeedAngular(), true);
            else if (ang < 0) this.player.getBody().applyAngularImpulse(player.getBreakSpeedAngular(), true);
        }

        world.step(1 / 45f, 6, 2);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        debugRenderer.render(world, camera.combined);
        //batch.begin();
        //batch.draw(img, 0, 0);
        //batch.end();
    }
}

//https://github.com/libgdx/libgdx/wiki/Box2d
//http://www.gabrielgambetta.com/fpm3.html