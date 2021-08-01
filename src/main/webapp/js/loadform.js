function onSelectReportType(ele){
    var form = $(ele).parent().parent();
    var label = $(form).find(".additional_msg");
    var select = $(form).find(".additional_msg_select");

    switch (ele.value) {
        case "DONATION":
        case "REQUEST":
            label.text("Resource:");
            select.find('option').remove();
            select.append($("<option></option>")
                .attr("value","")
                .text("Choose the resource type:"));
            selectValues = ['Water', 'Food', 'Money', 'Medicine', 'Clothing',
                'Rescue'];
            $.each(selectValues, function(index,value) {
                select.append($("<option></option>")
                    .attr("Value",value)
                    .text(value));
            });
            break;
        case "DAMAGE":
            label.text("Damage:");
            select.find('option').remove();
            select.append($("<option></option>")
                .attr("value","")
                .text("Choose the damage type"));
            selectValues = ['Pollution', 'Building Damage', 'Road Damage', 'Casualty',
                'Other'];
            $.each(selectValues, function(index,value) {
                select.append($("<option></option>")
                    .attr("value",value)
                    .text(value));
            });
            break;
        default:
            $(form).find(".additional_msg_div").css("visibility", "hidden");
            return;
    }
    $(form).find(".additional_msg_div").css("visibility", "visible");
}