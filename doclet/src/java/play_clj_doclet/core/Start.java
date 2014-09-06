package play_clj_doclet.core;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

import com.sun.javadoc.*;

public class Start {
	public static boolean start(RootDoc root) {
		RT.var("clojure.core", "require").invoke(Symbol.intern("play-clj-doclet.core"));
		Var generate = RT.var("play-clj-doclet.core", "generate-all!");
		generate.invoke(root);
		return true;
	}
}
