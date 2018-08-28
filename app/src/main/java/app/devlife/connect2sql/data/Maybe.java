package app.devlife.connect2sql.data;

/**
 * An extension of the Maybe paradigm from Haskell to allow the standardization
 * of response objects removing null as an appropriate response.
 *
 * @author javier.romero
 *
 * @param <T>
 * @param <E>
 */
public class Maybe<T, E> {

	private T mValue;

	private E mError;

	private Maybe() {
	}

	/**
	 * Create a {@link Maybe} object identified as an error with the error object
	 * @param error
	 * @return
	 */
	public static <T, E> Maybe<T, E> error(E error) {
		Maybe<T, E> maybe = new Maybe<T, E>();
		maybe.mError = error;
		return maybe;
	}

	/**
	 * Create a {@link Maybe} object with the response object given.
	 * @param value
	 * @return
	 */
	public static <T, E> Maybe<T, E> object(T value) {
		Maybe<T, E> maybe = new Maybe<T, E>();
		maybe.mValue = value;
		return maybe;
	}

	/**
	 * Check whether the object is present.
	 * @return
	 */
	public boolean isPresent() {
		return mValue != null;
	}

	/**
	 * Get the response object
	 * @return
	 */
	public T getObject() {
		return mValue;
	}

	/**
	 * Get the error object
	 * @return
	 */
	public E getError() {
		return mError;
	}
}