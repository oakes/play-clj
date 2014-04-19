package play_clj.g3d_physics;

import clojure.lang.IFn;
import com.badlogic.gdx.physics.bullet.collision.*;

public class ContactListener3D extends ContactListener {
	IFn started, ended;
	
	public ContactListener3D(IFn started, IFn ended) {
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
