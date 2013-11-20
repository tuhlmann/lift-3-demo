##Markdown Support

<div lift="ignore">This div will not be displayed
</div>

Lift 3 integrates the Markdown support from [Hoisted](https://github.com/hoisted/hoisted), giving you the
tools to write parts of your application directly in *Markdown*.

Under the hood, Lift uses the [Actuarius](http://henkelmann.eu/projects/actuarius/) parser.

You can also embed
    <div>...</div>

blocks on the page like this hero-unit

<div class="hero-unit">

  <form lift="form.ajax">
    <div lift="Markdown.parse">
      <h4>Say something in Markdown:</h4>
      <textarea class="form-control" name="markdown-text" style="width: 100%;" rows="5" cols="30"></textarea>
      <button style="margin-top:10px;" class="btn btn-default" name="submit">Submit</button>
    </div>
  </form>

  <h4>Preview:</h4>

  <div id="markdoown-preview"></div>

</div>


Lift's Markdown supports all the usual suspects, like

* Unordered Lists
    * with sublists


1. Numbered Lists
    1. With sub elements
    1. even more than one



### jQuery

* Autocomplete, the old way
* Autocomplete, Lift Roundtrip
* Typeahead, Lift Roundtrip

### AngularJS

* Angular page
* Small Angular app (Todo?), that shows CRUD with Lift's Roundtrip
* Example with Client/Server Lift-Actors

