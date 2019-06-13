## Share Code With the Client

Using the same language on the client and the server allows us to share code between
them. Doing so avoids duplication and reduces the chance for errors and unexpected
behaviors.

Let's update our project to extract validation into a namespace that will be cross-compiled
to both Clojure and ClojureScript. By doing that we'll be able to run validation client-side
and avoid calling the server entirely if the input data is invalid.

The first step will be to create a new source folder called `src/cljc`, and update the source
paths in `project.clj` to include it.

TODO: project.clj references

With the new souce folder in place, let's create a new namespace called `validation` in the
following file `src/cljc/guestbook/validation.cljc`. Note that the file extension is `cljc`,
this hints the compiler that this file will be cross-compiled to both Clojure and ClojureScript.

Next, let's move the validation code from the `guestbook.routes.home` namespace to the `guestbook.validation`
namespace:

TODO: reference validation

The updated `guestbook.routes.home` namespace will now require the `validate-message` function
from the `guestbook.validation` namespace:

TODO: reference new home ns declaration

Let's restart the application to confirm that server-side code still works correctly. Now, we
can turn our attention to the client where we can start using the `validate-message` function
to ensure taht the message content is valid before sending it to the server.

We'll require the `guestbook.validation` namespace the same way we did earlier:

TODO: code

Then we'll add the validation check in the `send-message!` function:

TODO: code

The updated code will attempt to validate the message before making the Ajax call. If
any errors are returned by the `validate-message` function those will be set in
the `errors` atom shortcutting the need for server-side validation.

The advantage of this approach is that we're able to reduce server load by avoiding
making Ajax calls with invalid data. Of course, the server will still have the final
say since it's using the same validation logic.
