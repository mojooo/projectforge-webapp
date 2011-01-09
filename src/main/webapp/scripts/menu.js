$(function() {
	// hier verknüpfe ich die listen und konfiguriere die sortable-funktion.
	$("#normal #nav ul li").draggable( {
		connectToSortable : "#personal",
		placeholder : "ui-state-highlight",
		helper : 'clone'
	}).disableSelection();

	$("#personal").sortable( {
		placeholder : "ui-state-highlight",
		change: function(event, ui) {
			$(".remover").remove();
			$("ul#personal li a").prepend( '<span class="remover"> 2 </span>');
			// @kai: call a function here to serialize and save the personal bar:
			// z.B. savePersonal();
		}
	}).disableSelection();
	
	$( "#personal" ).bind( "sortreceive", function(event, ui) {
		console.log("--");
		$("ul#personal li").each(function(index){
			console.log($(this).attr("id"));
			if ( ui.item.attr("id") == $(this).attr("id")){
				console.log( ui.item.attr("id") + "is a duplicate");
				ui.item.remove();
				
			}	
			
			
		});
	
		
		});
	

	// @kai: Das ist neu, beim Laden muss man natürlich das Sortable disablen...
	$("#personal, #nav ul").sortable("disable");

	// @kai: dies aktiviert die remover
	$('.remover').live('click', function() {
		$(this).parents("li").remove();
		return false;
	});

});
