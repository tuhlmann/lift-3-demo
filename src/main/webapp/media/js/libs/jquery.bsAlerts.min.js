/**
  * A jQuery plugin for displaying Bootstrap alerts via jQuery events.
  */
;(function($, window, document, undefined) {
  "use strict";

  /* BsAlerts class definition
   * ========================== */
  var BsAlerts = function(element, options) {
    var self = this, fadeTimer;

    self.element = element;
    self.options = $.extend({}, $.fn.bsAlerts.defaults, options);

    $(document).on("add-alerts", function() {
      var alerts = Array.prototype.slice.call(arguments, 1);
      self.addAlerts(alerts);
    });

    $(document).on("clear-alerts", function() {
      self.clearAlerts();
    });

    $.each(this.options.ids.split(","), function(ix, alert_id) {
      var id = alert_id.trim();
      if (id.length > 0) {
        var evtId = "set-alert-id-"+id;
        $(document).on(evtId, function() {
          var msgs = Array.prototype.slice.call(arguments, 1);
          self.addAlerts(msgs);
        });
      }
    });

    self.clearAlerts = function() {
      $(this.element).html("");
    };

    self.addAlerts = function(data) {
      var alerts = splitAlerts([].concat(data));
      self.addAlertsToContainer(alerts.errs);
      self.addAlertsToContainer(alerts.warns);
      self.addAlertsToContainer(alerts.infos);
      self.addAlertsToContainer(alerts.succs);

      var fadeInt = parseInt(self.options.fade, 10);
      if (!isNaN(fadeInt) && fadeInt > 0) {
        clearTimeout(fadeTimer);
        fadeTimer = setTimeout(self.fade, fadeInt);
      }
    };

    self.fade = function() {
      $("[data-alerts-container]").fadeOut('slow', function() {
        $(this).remove();
      });
    };

    self.buildNoticeContainer = function(msgs) {
      if (msgs.length > 0) {

        var priority = bsPriority(msgs[0].priority);

        var $dismissBtn = $('<button/>', {
          'type':'button',
          'class':'close',
          'data-dismiss':'alert'
        }).html('&times;');

        var $ul = $("<ul/>");
        self.attachLIs($ul, msgs);

        var $container = $("<div/>", {
          "data-alerts-container": priority,
          "class": "alert alert-"+priority
        });

        $container.append($dismissBtn);

        var title = this.options.titles[priority];

        if (title && title.length > 0) {
          $container.append($("<strong/>").html(title));
        }

        $container.append($ul);

        return $container;
      }

      return null;
    };

    self.addAlertsToContainer = function(msgs) {
      if (msgs.length > 0) {
        var $ele = $(this.element);
        var priority = bsPriority(msgs[0].priority);
        var $container = $("[data-alerts-container='"+priority+"']", $ele);

        if ($container.length > 0) {
          var $ul = $container.find("ul");
          self.attachLIs($ul, msgs);
        }
        else {
          $container = self.buildNoticeContainer(msgs);
          $ele.append($container);
        }
      }
    };

    self.attachLIs = function($ul, msgs) {
      $.each(msgs, function(ix, it) {
        $ul.append($("<li/>").html(it.message));
      });
    };
  };

  // "private" funcs
  function splitAlerts(alerts) {
    return {
      errs: $.grep(alerts, function(it) {
        return (it.priority === "error" || it.priority === "danger");
      }),
      warns: $.grep(alerts, function(it) {
        return it.priority === "warning";
      }),
      infos: $.grep(alerts, function(it) {
        return (it.priority === "notice" || it.priority === "info");
      }),
      succs: $.grep(alerts, function(it) {
        return it.priority === "success";
      })
    };
  }

  function bsPriority(it) {
    if (it === "notice") {
      return "info";
    }
    else if (it === "error") {
      return "danger";
    }
    return it;
  }

  /* BsAlerts plugin definition
   * ===================== */

  var old = $.fn.bsAlerts;

  $.fn.bsAlerts = function(option) {
    return this.each(function () {
      var $this = $(this),
          data = $this.data('bsAlerts'),
          options = typeof option === 'object' && option;

      if (!data) {
        $this.data('bsAlerts', (data = new BsAlerts(this, options)));
      }
      if (typeof option === 'string') {
        data[option]();
      }
    });
  };

  $.fn.bsAlerts.Constructor = BsAlerts;

  $.fn.bsAlerts.defaults = {
    titles: {},
    ids: '',
    fade: '0'
  };

  /* BsAlerts no conflict
   * =============== */

  $.fn.bsAlerts.noConflict = function () {
    $.fn.bsAlerts = old;
    return this;
  };


  /* BsAlerts data-api
   * ============ */

  $(document).ready(function () {
    $('[data-alerts="alerts"]').each(function () {
      var $ele = $(this);
      $ele.bsAlerts($ele.data());
    });
  });

}(jQuery, window, document));
