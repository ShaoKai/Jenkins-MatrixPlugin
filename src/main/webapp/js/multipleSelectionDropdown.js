
$jq(document).on('DOMNodeInserted', 'select[multiple]', function(){
    $jq('select[multiple]').each(function(){
        var dropdown = $jq(this);

        if($jq(this).next().length>0) return; // Chosen already initialized

        var name = /_\.(\w{1,20})Dropdown/.exec(dropdown.attr('name'))[1];
        var self = dropdown.closest("table").parent().find("[name$="+name+"]");
        var defaultValues = self.val().split(",");

        console.debug("==============================");
        console.debug("dropdown     : " + dropdown.length);
        console.debug("dropdown     : " + dropdown.attr('name'));
        console.debug("self         : " + self.attr('name'));
        console.debug("defaultValue : " + self.val());
        console.debug("==============================");

        // initialize the default values
        dropdown.find('option')
            .filter(function() {
                return ($jq.inArray($jq(this).val(), defaultValues)>-1)
            })
            .prop('selected', true);

        dropdown
            .addClass('chosen')
            .attr("data-placeholder", " ")
            .chosen({
                limit:0
            })
            .change(function(){
                // change the target field while changing the dropdown list
                var val = $jq(this).val().join(",");
                console.log("Original value is " + self.val());
                console.log("Replace value with " + val);
                self.val(val);
            });

    });
});