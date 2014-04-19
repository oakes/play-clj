package play_clj.g3d_physics;

import clojure.lang.IFn;

public class ContactListener extends com.badlogic.gdx.physics.bullet.collision.ContactListener {
	IFn started, ended;
	
	public ContactListener(IFn started, IFn ended) {
		this.started = started;
		this.ended = ended;
	}
	
	public void onContactStarted(btCollisionObject a, btCollisionObject b) {
		started.invoke(a, b);
	}

	public void onContactEnded(btCollisionObject a, btCollisionObject b) {
		ended.invoke(a, b);
	}
}
