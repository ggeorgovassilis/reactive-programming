package reactive;

/**
 * Callback interface for {@link Promise#whenAvailable(Callback)}
 *
 * @param <T>
 * @author george georgovassilis
 */

public interface Callback<T> {

		/**
		 * Called when a value is available
		 * @param success
		 */
		void set(T success);

		/**
		 * Called when an error occurred
		 * @param e
		 */
		void fail(Exception e);

}
