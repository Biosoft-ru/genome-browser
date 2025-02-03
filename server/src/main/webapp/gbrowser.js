function login(callback)
{
    var loginData = {
            username: "",
            password: ""
        };
    queryBioUML("web/login", loginData, 
        function(data)
        {
            if(callback)
                callback();
        }, function(data)
        {
            //TODO: login as anonymous failed
        });
}

var appInfo = {
        name: "Genome browser",
        edition: "web edition",
        version: "1.0",
        ensemblPath: "databases/EnsemblHuman100",
        serverPath: "../biouml/"
};

function loadContent()
{
    initBioUMLGlobals();
    initTracks();
    initUIMode(true);
    loadGenomeBrowser();
    initButtons();
}

function initBioUMLGlobals()
{
    defaultTitle = "Genome browser BioUML";
    perspective = {};
}

var ensemblTracksPath = appInfo.ensemblPath.concat("/Tracks/");
var ensemblTracks = {};
var enabledTracks = {};
function initTracks()
{
    ensemblTracks["GC content"] = ensemblTracksPath.concat("GC content");
    ensemblTracks["Genes"] = ensemblTracksPath.concat("Genes");
    ensemblTracks["Extended genes"] = ensemblTracksPath.concat("ExtendedGeneTrack");
    ensemblTracks["Repeats"] = ensemblTracksPath.concat("Repeats");
    ensemblTracks["Transcripts"] = ensemblTracksPath.concat("Transcripts");
    ensemblTracks["Karyotype"] = ensemblTracksPath.concat("Karyotype");
    //ensemblTracks["Variations"] = ensemblPath.concat("Variations");
}

var activeDoc;
var karyoDoc;
function loadGenomeBrowser(){
    var sequencePath = appInfo.ensemblPath.concat("/Sequences/chromosomes GRCh38");
    var mainDiv = $("#gbrowser");
    mainDiv.empty();

    CreateSequenceDocument(sequencePath, "gb_main", function (sequenceDoc) {
        sequenceDoc.initialTracks = [ensemblTracks["Genes"], ensemblTracks["GC content"]];
        sequenceDoc.open(mainDiv);
        activeDoc = sequenceDoc;
        let pane = $(activeDoc.viewPanesContainerDiv);
        let docDiv = pane.parents().find("#gb_main");
        let docContDiv = pane.parents().find("#gb_main_container");
        docContDiv.css("width", "100%").css("border", "none");
        //docDiv.css("height", "50%");
        docDiv.css("width", "100%");
        activeDoc.sequenceContainerDiv.resize();
        activeDoc.sequenceDocument.bind("sequenceLoaded", function(){
            mLoaded = true;
            adjustRegionSelector(activeDoc, karyoDoc)
            var tr = _.find(activeDoc.enabledTracks, function(track)
            {
                return track.de == ensemblTracks["GC content"]; 
            });
            if(tr)
                enabledTracks["GC content"] = tr.id;
            initViewPartsGB();
        });
        activeDoc.sequenceDocument.bind("positionChanged", function(){
            adjustRegionSelector(activeDoc, karyoDoc);  
        });
        activeDoc.chromosomeSelector.change(function(){
            karyoDoc.setPosition(activeDoc.chromosomeSelector.val()+":");
        });
        
    });
    
    var karyoDiv = $("#gbrowser-karyo");
    karyoDiv.empty();
    CreateSequenceDocument(sequencePath, "gb_karyo", function (sequenceDoc) {
        sequenceDoc.initialTracks = [ensemblTracks["Karyotype"]];
        sequenceDoc.open(karyoDiv);
        karyoDoc = sequenceDoc;
        
        let pane = $(activeDoc.viewPanesContainerDiv);
        
        karyoDoc.viewPanesContainerDiv.draggable();
        karyoDoc.viewPanesContainerDiv.draggable('disable');
        karyoDoc.viewPanesContainerDiv.css("opacity", "1.0");
        
        karyoDoc.positionInformer.positionInformerText.hide();
        //karyoDoc.positionInformer.positionInformerLine.hide();
        
        let docDiv = pane.parents().find("#gb_karyo");
        let docContDiv = pane.parents().find("#gb_karyo_container");
        docContDiv.css("width", "100%").css("border", "none");
        
        docDiv.css("height", "100px");
        docDiv.css("width", "100%");
        
        karyoDoc.searchSelector = undefined;
        karyoDoc.sequenceContainerDiv.children("#gb_karyo_controls").hide();
        
        karyoDoc.sequenceContainerDiv.resize();
        karyoDoc.sequenceDocument.bind("sequenceLoaded", function(){
            karyoDoc.zoomFull();
            kLoaded = true;
            adjustRegionSelector(activeDoc, karyoDoc);
            karyoDoc.sequenceContainerDiv.find(".trackLabel").html("Chromosome");
        });
        
    });
}

var kLoaded = false;
var mLoaded = false;

function adjustRegionSelector(mainDoc, karyoDoc)
{
    if(!kLoaded || !mLoaded)
        return;
    var trackID = _.keys(karyoDoc.enabledTracks)[0];
    var k_top = 2;
    var k_left = Math.max(2,pos2pixel(mainDoc.from, karyoDoc.positionInformer));
    var k_width = pos2pixel(mainDoc.positionInformer.to - mainDoc.from, karyoDoc.positionInformer) - 2;
    //var k_width = (Math.ceil((mainDoc.positionInformer.to - mainDoc.from) / karyoDoc.positionInformer.pixelWidth)-0.5)*karyoDoc.positionInformer.pixelWidth * karyoDoc.positionInformer.zoom;
    var k_heigth = Math.min(18, karyoDoc.panes[trackID].height - 8);
    showRegionSelector(karyoDoc.panes[trackID].canvasDiv, k_left, k_top, k_width, k_heigth);
    
}

function pos2pixel (pos, positionInformer)
{
    return ( Math.ceil( pos / positionInformer.pixelWidth ) - 0.5 ) * positionInformer.pixelWidth * positionInformer.zoom;
}

function CreateSequenceDocument (name, docId, callback)
{
    callback(new SequenceDocument(name, {}, docId));
}

var regionSelectorDiv = null;
function removeRegionSelector()
{
    if(regionSelectorDiv)
    {
        regionSelectorDiv.remove();
        regionSelectorDiv = null;
    }
}

function showRegionSelector(parent, x, y, width, height)
{
    removeRegionSelector();
    var selectorDiv = $("<div class='region_selector'></div>").css('position', 'absolute');
    var selectorDivDotes = $("<div class='region_selector_dotes'></div>").css('opacity', .8);
    selectorDiv.append(selectorDivDotes);
    parent.append(selectorDiv);
    selectorDiv.css('left', x - 2).css('top', y - 2).css('width', width + 4).css('height', height + 4);
    selectorDiv.children(".region_selector_dotes").css('left', x).css('top', y).css('width', width).css('height', height);
    selectorDiv.show();
    regionSelectorDiv = selectorDiv;
    return selectorDiv;
}

var viewParts = new Array();
var vps =  "track.finder";
function initViewPartsGB()
{
    var viewPartTabs = $(document.getElementById("gb-viewPartTabs"));
    initBSAViewParts();

    var tabs = viewPartTabs.tabs(
    {
        show: function(event, ui)
        {
            if (opennedViewPartId != null) 
            {
                var vp = lookForViewPart(opennedViewPartId);
                if (vp != null) 
                {
                    vp.save();
                }
            }
            opennedViewPartId = $(ui.panel).attr('data-id');
            var vp = lookForViewPart(opennedViewPartId);
            if (vp != null) 
            {
                updateViewPartsToolbarGB(vp);
                if(vp.show)
                    vp.show(activeDoc);
            }
            viewPartTabs.triggerHandler("resize");
        }
    });
    this.viewPartToolbar = $('<div class="ui-corner-all ui-helper-clearfix tabs-shifter" style="min-width: 115px; left: 0px; top: 2px; position:absolute; z-index:1;"></div>');
    tabs.find('.ui-tabs-nav').append(this.viewPartToolbar);
    
    for (var vpi = 0; vpi < viewParts.length; vpi++) 
    {
        var viewPart = viewParts[vpi];
        (function(viewPart)
        {
            viewPartTabs.append(viewPart.tabDiv);
            viewPartTabs.tabs('add', getViewPartSelector(viewPart.tabId), viewPart.tabName);
            viewPart.explore(activeDoc);
            if ((vps != null) && (vps == viewPart.tabId)) 
            {
                selectViewPartGB(vps);
                vps = null;
            }
        })(viewPart);
    }
}

function updateViewPartsToolbarGB(viewPart)
{
    this.viewPartToolbar.empty();
    var block = $('<div class="fg-buttonset ui-helper-clearfix" style="margin: 3px 0 0 2px;"></div>');
    
    if (viewPart && viewPart.initActions) 
    {
        viewPart.initActions(block);
    }
    
    this.viewPartToolbar.append(block);
    var leftToolbarShift = Math.max(block.children().length*22 + 5, 115)  ;
    $('#gb-viewPartTabs > ul').css("padding-left", leftToolbarShift + "px");
}

function selectViewPartGB(id)
{
    viewPartToSelect = id;
    var selector = getViewPartSelector(id);
    var anchors = $('#gb-viewPartTabs > ul > li > a');
    var offset = 0;
    if(anchors.length > 0 && anchors.get(0).getAttribute("href") == "#")
        offset = 1;
    for(var index = 0; index < anchors.length; index++)
    {
        
        if(anchors.get(index).getAttribute("href") === selector)
        {
            $(document.getElementById("gb-viewPartTabs")).tabs('select', index-offset);
        }
    }
}



var fullUI = true;
function initUIMode(fu) 
{
    if(!fu || fu  === 'false')
    {
        fullUI = false;
        $("#gb-bottomPane").hide();
        $("#gb-topPane").height("100%").trigger('resize');
    } else
    {
        fullUI = true;
        $("#gb-topPane").height("70%");
        $("#gb-bottomPane").height("30%").show();
        $("#gb-mainPane").trigger('resize');
    }
}

function initButtons()
{
    $("#gb-button-zoomin").click(function (e) {
        if (activeDoc != null)
        {
            activeDoc.zoomIn();
        }
    });
    $("#gb-button-zoomout").click(function (e) {
        if (activeDoc != null)
        {
            activeDoc.zoomOut();
        }
    });
    $("#gb-button-forward").click(function (e) {
        if (activeDoc != null)
        {
            activeDoc.shiftForward(15);
        }
    });
    $("#gb-button-back").click(function (e) {
        if (activeDoc != null)
        {
            activeDoc.shiftBackward(15);
        }
    });
    $("#gb-button-overview").click(function (e) {
        if (activeDoc != null)
        {
            activeDoc.zoomFull();
        }
    });
    $("#gb-button-detailed").click(function (e) {
        if (activeDoc)
        {
            activeDoc.zoomDetailed();
        }
    });
    $("#gb-button-default").click(function (e) {
        if (activeDoc != null)
        {
            activeDoc.zoomDefault();
        }
    });
    
    $(".header").click(function () {

        $header = $(this);
        $content = $header.next();
        $content.slideToggle(300, function () {
            $(".headeritem").html(function () {
                return $content.is(":visible") ? "&ndash;" : "+";
            });
        });

    });
    
    $(window).resize(function(event){
        if(!$.isWindow(event.target)) return;
        karyoDoc.sequenceContainerDiv.resize();
        karyoDoc.zoomFull();
        activeDoc.sequenceContainerDiv.resize();
    });
    
    $("#gb-button-toggleui").click(function (e) {
        initUIMode(!fullUI);
    });

}

