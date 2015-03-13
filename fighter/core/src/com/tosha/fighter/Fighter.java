package com.tosha.fighter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
    Body body;
    Body groundBody;

    @Override
    public void create() {
        //batch = new SpriteBatch();
        //img = new Texture("badlogic.jpg");
        Box2D.init();
        world = new World(new Vector2(0, -20), true);
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();
        //camera.setToOrtho(false, 800, 480);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set(100, 300);
        body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(12f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 3.0f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

        Fixture fixture = body.createFixture(fixtureDef);

        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(new Vector2(0, 10));

        groundBody = world.createBody(groundBodyDef);

        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(camera.viewportWidth, 10.0f);
        groundBody.createFixture(groundBox, 0.0f);
        groundBox.dispose();

        circle.dispose();
    }

    @Override
    public void render() {
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