(function(window, $) {
  "use strict";

  window.App = (function() {
    // the instance to return
    var inst = {};

    inst.init = function(settings) {
      inst.settings = settings;
    };

    /**
      * A convenience function for parsing string namespaces and
      * automatically generating nested namespace objects.
      *
      * Example:
      * App.namespace('modules.module2');
      *
      */
    inst.namespace = function(ns_string) {
      var parts = ns_string.split('.'),
          parent = inst,
          pl;

      pl = parts.length;
      for (var i = 0; i < pl; i++) {
        // create a property if it doesnt exist
        if (typeof parent[parts[i]] === 'undefined') {
          parent[parts[i]] = {};
        }
        parent = parent[parts[i]];
      }
      return parent;
    };

    return inst;
  }());
})(this, jQuery);

