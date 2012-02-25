

InstrBrowser {

	/*
		toolbarFunc: arg layout,instr
	*/

    var <>toolbarFunc,lv,frame;
    var instrs,ugenInstrs,<>rate=nil,<>showUGenInstr=true,<>inputSpec,<>outputSpec;

    *new { arg toolbarFunc,showUGenInstr=false;
        ^super.newCopyArgs(toolbarFunc).showUGenInstr_(showUGenInstr).init
    }
    gui { arg layout,bounds;
        this.guiBody( layout.asFlowView(bounds ?? {Rect(100,0,1000,Window.screenBounds.height - 50)} ) );
    }
    guiBody { arg layout;
        var search,rateFilter;
        search = TextField(layout,Rect(0,0,240,17));
        search.string = "";
        search.keyDownAction = {true};
        search.action = {this.search(search.value)};

        rateFilter = PopUpMenu(layout,120@17);
        rateFilter.items = ["all","audio","control","fft","stream","player","demand"];
        rateFilter.action = {
            if(rateFilter.value == 0,{
                 this.rate = nil;
                 this.init.refresh;
            },{
                this.rate = rateFilter.item.asSymbol;
                 this.init.refresh;
            })
        };
        layout.startRow;
        layout.horz({ arg layout;
            lv = ListView(layout,250@layout.bounds.height);
            lv.items = instrs;
            lv.mouseUpAction_({ arg view, x, y, modifiers, buttonNumber, clickCount;
                this.focus(lv.items[lv.value]);
            });
            lv.beginDragAction = {
                lv.items[lv.value]
            };
            frame = layout.flow({ arg layout; },(layout.bounds.width-254)@layout.bounds.height);
        },layout.indentedRemaining)
        .background_(Color(0.83582089552239, 0.83582089552239, 0.83582089552239));

        Updater(Instr,{
            this.init
        }).removeOnClose(layout);
    }
    init {
        instrs = Instr.leaves;
        if(showUGenInstr,{
	        instrs = instrs ++ UGenInstr.leaves(this.rateMethod);
        });
        if(rate.notNil,{
            instrs = instrs.select({ arg ins; ins.outSpec.notNil and: {ins.outSpec.rate == rate}})
        });
        if(inputSpec.notNil,{
	        instrs = instrs.select({ arg ins; 
		        ins.specs.any({ arg sp; 
			        // ignore numChannels mismatches
			        sp.class === inputSpec.class and: { sp.respondsTo('numChannels') or: {sp == inputSpec} }
			    }) 
		   });
        });
        if(outputSpec.notNil,{
	        instrs = instrs.select({ arg ins; 
			        // ignore numChannels mismatches
			   ins.outSpec.class === outputSpec.class and: { ins.outSpec.respondsTo('numChannels') or: {ins.outSpec == outputSpec} }
		   });
        });
        
        instrs = instrs.collect(_.dotNotation).sort;
    }
    rateMethod {
        ^rate.switch(
                nil,\ar,
                \audio,\ar,
                \control,\kr,
                \demand,\new,
                \fft,\new,
                \stream,\stream,
                \player,\player
                );
    }
    allInstr {
	    ^instrs
    }
    refresh {
	    lv.items = instrs;
	    lv.refresh;
    }
    // only if gui is active
    search { arg q;
        var base;
        if(q != "",{
            lv.items = (instrs.select(_.containsi(q)));
        },{
            lv.items = instrs
        });
        lv.refresh;
    }
    focus { arg instrname;
        var instr,ic,rr;
        frame.removeAll;
        
        if(instrname.isNil,{
            ^this
        });
        ic = instrname.asSymbol.asClass;
        if(showUGenInstr and: {ic.notNil} and: {Instr.at([ic.name.asString]).isNil}) {
            rr = this.rateMethod;
            instr = UGenInstr(ic,rr)
        }{
            instr = Instr(instrname);
        };
        toolbarFunc.value(frame,instr);
        frame.startRow;
        frame.scroll({ arg frame;
	        frame.flow({ arg frame;
	            instr.gui(frame);
	        },frame.bounds.moveTo(0,0))
        },frame.indentedRemaining);
    }
}


