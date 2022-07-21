"use strict";(self.webpackChunkwiki=self.webpackChunkwiki||[]).push([[852],{3905:(e,t,n)=>{n.d(t,{Zo:()=>k,kt:()=>p});var r=n(7294);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function a(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?a(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):a(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},a=Object.keys(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var s=r.createContext({}),c=function(e){var t=r.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},k=function(e){var t=c(e.components);return r.createElement(s.Provider,{value:t},e.children)},u={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},h=r.forwardRef((function(e,t){var n=e.components,o=e.mdxType,a=e.originalType,s=e.parentName,k=l(e,["components","mdxType","originalType","parentName"]),h=c(n),p=o,d=h["".concat(s,".").concat(p)]||h[p]||u[p]||a;return n?r.createElement(d,i(i({ref:t},k),{},{components:n})):r.createElement(d,i({ref:t},k))}));function p(e,t){var n=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var a=n.length,i=new Array(a);i[0]=h;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l.mdxType="string"==typeof e?e:o,i[1]=l;for(var c=2;c<a;c++)i[c]=n[c];return r.createElement.apply(null,i)}return r.createElement.apply(null,n)}h.displayName="MDXCreateElement"},5644:(e,t,n)=>{n.d(t,{Z:()=>r});const r={getting_started:{intro:"/Linkit/docs/Getting Stared/Naming/Introduction",framework_architecture:"/Linkit/docs/Getting Stared/Naming/Framework Architecture",first_network:"/Linkit/docs/Getting Stared/Naming/Your First Network"},gnom:{naming:{intro:"/Linkit/docs/GNOM/Naming/Introduction",nol:"/Linkit/docs/GNOM/Naming/Network Object Linkers",nor:"/Linkit/docs/GNOM/Naming/Network Object Reference",no:"/Linkit/docs/GNOM/Naming/What is a network object"},caches:{connected_objects:{bhv_lang:{}}},persistence:{intro:"/Linkit/docs/GNOM/persistence/intro"}}}},9521:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>k,contentTitle:()=>s,default:()=>p,frontMatter:()=>l,metadata:()=>c,toc:()=>u});var r=n(7462),o=(n(7294),n(3905));n(2389);var a=n(5644);function i(e){return"pathname:///scaladoc/"+e.toString().replace(/\./g,"/")}const l={sidebar_position:1,sidebar_label:"Network Objects"},s="Network Objects",c={unversionedId:"GNOM/Naming/What Is A Network Object",id:"GNOM/Naming/What Is A Network Object",title:"Network Objects",description:"What is a network object",source:"@site/docs/GNOM/Naming/What Is A Network Object.mdx",sourceDirName:"GNOM/Naming",slug:"/GNOM/Naming/What Is A Network Object",permalink:"/Linkit/docs/GNOM/Naming/What Is A Network Object",draft:!1,tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1,sidebar_label:"Network Objects"},sidebar:"tutorialSidebar",previous:{title:"Introduction",permalink:"/Linkit/docs/GNOM/Naming/"},next:{title:"Network Object References",permalink:"/Linkit/docs/GNOM/Naming/Network Object Reference"}},k={},u=[{value:"What is a network object",id:"what-is-a-network-object",level:2},{value:"How to create a Network Object",id:"how-to-create-a-network-object",level:2},{value:"So what happens if an engine sends a network object that is not referenced on the targeted engine ?",id:"so-what-happens-if-an-engine-sends-a-network-object-that-is-not-referenced-on-the-targeted-engine-",level:4},{value:"Use cases",id:"use-cases",level:2},{value:"Serialize non-serializable objects",id:"serialize-non-serializable-objects",level:3},{value:"Relative Objects",id:"relative-objects",level:3},{value:"Reinjecting same objects references",id:"reinjecting-same-objects-references",level:3}],h={toc:u};function p(e){let{components:t,...n}=e;return(0,o.kt)("wrapper",(0,r.Z)({},h,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"network-objects"},"Network Objects"),(0,o.kt)("h2",{id:"what-is-a-network-object"},"What is a network object"),(0,o.kt)("p",null,(0,o.kt)("orange",null,"Network Objects"),"are an essential part of the ",(0,o.kt)("orange",null,"GNOM")," System.",(0,o.kt)("br",null),"Their only particularity is that they are bound with a ",(0,o.kt)("a",{href:a.Z.gnom.naming.nor},"reference"),".",(0,o.kt)("br",null),"This concept is really ",(0,o.kt)("orange",null,"simple"),", but this allows the framework to perform ",(0,o.kt)("orange",null,"packet stream size optimisation"),", by sending the reference instead of the object itself, and, on top of that, sending the reference of an object allows to solve ",(0,o.kt)("orange",null,"recurrent issues")," when developing a network based system, and eases the manipulation of ",(0,o.kt)("i",null,"network")," objects between ",(0,o.kt)("orange",null,"remote engines")),(0,o.kt)("h2",{id:"how-to-create-a-network-object"},"How to create a Network Object"),(0,o.kt)("p",null,"To create a Network Object, you'll need 3 things:"),(0,o.kt)("ol",null,(0,o.kt)("li",{parentName:"ol"},"Define (or use) a ",(0,o.kt)("inlineCode",{parentName:"li"},"NetworkObjectReference"),". Take a look at the ",(0,o.kt)("a",{href:a.Z.gnom.naming.nor},"Network Object Reference")," page to learn more.\nwe'll use ",(0,o.kt)("inlineCode",{parentName:"li"},"MyNetworkObjectReference")," as an example."),(0,o.kt)("li",{parentName:"ol"},"Define your network object class by extending the ",(0,o.kt)("a",{href:i("fr.linkit.api.gnom.reference.NetworkObject")},"NetworkObject")," interface.")),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},"class MyNetworkObject(override val reference: MyNetworkObjectReference) extends NetworkObject[MyNetworkObjectReference]\n")),(0,o.kt)("ol",{start:3},(0,o.kt)("li",{parentName:"ol"},"Put the instances of this class in a ",(0,o.kt)("a",{href:i("fr.linkit.api.gnom.reference.linker.NetworkObjectLinker")},"Network Object Linker "),". You can define your own Network Object Linker, or use the ",(0,o.kt)("a",{href:i("fr.linkit.api.gnom.reference.linker.DefaultNetworkObjectLinker")},"DefaultNetworkObjectLinker")," as a linker for your objects. You can also prefer the ",(0,o.kt)("a",{href:i("fr.linkit.api.gnom.reference.ContextObjectLinker")},"ContextObjectLinker "),"of your ",(0,o.kt)("a",{href:i("fr.linkit.api.gnom.packet.channel.PacketChannel")},"PacketChannel"),".",(0,o.kt)("br",null))),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'val ref = MyNetworkReference("tommy")\nval myNetworkObject = new MyNetworkObject(ref)\nval defaultLinker = network.gnol.defaultNOL\ndefaultLinker.save(myNetworkObject)\n')),(0,o.kt)("p",null,"Did not understand a thing of the last sentence ? take a look at ",(0,o.kt)("a",{href:a.Z.gnom.naming.nol},"Network Object Linker page"),"\nto understand what is a network object linker, and know what linker you should use depending on the context and your needs."),(0,o.kt)("p",null,"That's it. Now the framework knows your object,\nand if another engine also have an object referenced at ",(0,o.kt)("inlineCode",{parentName:"p"},'MyNetworkReference("tommy")'),",\nno matters the used linker, the referencing will work."),(0,o.kt)("h4",{id:"so-what-happens-if-an-engine-sends-a-network-object-that-is-not-referenced-on-the-targeted-engine-"},"So what happens if an engine sends a network object that is not referenced on the targeted engine ?"),(0,o.kt)("p",null,"The handling of a network object whose reference is not registered on targeted engine is different depending on the type\nof Network Object involved:"),(0,o.kt)("ul",null,(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("u",null,"Regular / Dynamic Network Objects"),(0,o.kt)("a",null," "),"The object is sent on the distant engine, then the distant engine will register the object's reference."),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("u",null,"Static Network Objects"),(0,o.kt)("a",null," "),"Because they are intended to be static over the network, if an engine receives an unregistered static object reference, it'll throw an exception.")),(0,o.kt)("h2",{id:"use-cases"},"Use cases"),(0,o.kt)("h3",{id:"serialize-non-serializable-objects"},"Serialize non-serializable objects"),(0,o.kt)("p",null,"The ",(0,o.kt)("orange",null,"current persistence system")," is already able to handle objects that are ",(0,o.kt)("orange",null,"not designed")," to support ",(0,o.kt)("orange",null,"serialization"),", and is enough ",(0,o.kt)("orange",null,"configurable")," to let the user define how an object should be ",(0,o.kt)("orange",null,"serialised/deserialized"),"if the default 'field copy/paste' method is not convenient.",(0,o.kt)("br",null),"however, there is still enough reason why serialization/deserialization of an object can be undesirable:"),(0,o.kt)("p",null,"For example, the trait ",(0,o.kt)("a",{href:i("fr.linkit.api.application.ApplicationContext")},"ApplicationContext"),"'s implementations objects are just ",(0,o.kt)("orange",null,"too big")," to get send threw the ",(0,o.kt)("orange",null,"network"),". Moreover, ",(0,o.kt)("orange",null,"they are intended to have only one instance in the JVM"),". Without the GNOM (General Network's Objects Management), if the ",(0,o.kt)("orange",null,"Application object")," is set into a packet, and then the packet is send to any engine, the the ",(0,o.kt)("a",{href:a.Z.gnom.persistence.intro},"persistence")," system will handle the",(0,o.kt)("a",{href:i("fr.linkit.api.application.ApplicationContext")}," Application")," object as such, and so, the receiver of the packet will end up with two applications objects, ",(0,o.kt)("orange",null,"which is problematic because only one ApplicationContext is intended"),". Thanks to the ",(0,o.kt)("orange",null,"GNOM System"),", this kind of ",(0,o.kt)("orange",null,"conceptual problem")," can be ",(0,o.kt)("orange",null,"avoided")," as only the ",(0,o.kt)("orange",null,"application's reference")," will be sent in the socket, and then the receiver's GNOM system will replace the received reference by its Application object."),(0,o.kt)("h3",{id:"relative-objects"},"Relative Objects"),(0,o.kt)("p",null,"Well, as ",(0,o.kt)("orange",null,"NetworkObjects")," are bound to their ",(0,o.kt)("orange",null,"reference"),", this means that a ",(0,o.kt)("orange",null,"Network Object Reference")," (or ",(0,o.kt)("orange",null,"NOR"),") can point to a ",(0,o.kt)("orange",null,"network object ")," that can be ",(0,o.kt)("orange",null,"different")," depending on the machine.",(0,o.kt)("br",null)),(0,o.kt)("orange",null,"As an example"),", let's assume that we are making a multiplayer game.",(0,o.kt)("br",null),(0,o.kt)("p",null,"For this game, we define a trait ",(0,o.kt)("inlineCode",{parentName:"p"},"Player extends NetworkObject[PlayerReference]")," and two implementations of Player:"),(0,o.kt)("ul",null,(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"ControlledPlayer"),", relative to each connected ",(0,o.kt)("orange",null,"engine"),", which is the player object of the ",(0,o.kt)("orange",null,"human behind the screen")," that can actually\ncontrol the player"),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"RemotePlayer"),", for remote players that are connected to our session.")),(0,o.kt)("p",null,"We can define a ",(0,o.kt)("orange",null,"reference")," (such as ",(0,o.kt)("inlineCode",{parentName:"p"},"@session/players/controller"),") that all ",(0,o.kt)("inlineCode",{parentName:"p"},"ControlledPlayer")," objects of the ",(0,o.kt)("orange",null,"network"),"\nwill be bound to (invoking ",(0,o.kt)("inlineCode",{parentName:"p"},"ControlledPlayer#reference")," will return the reference ",(0,o.kt)("inlineCode",{parentName:"p"},"@session/players/controller"),"). using this trick,\nonce the ",(0,o.kt)("inlineCode",{parentName:"p"},"ControlledPlayer")," object is sent to an engine, as Network Object are replaced by their reference during the serialization, the\nengines that will receive the reference will take the object referenced at ",(0,o.kt)("inlineCode",{parentName:"p"},"@session/players/controller"),", which will result\nto its own ",(0,o.kt)("inlineCode",{parentName:"p"},"ControlledPlayer")," instance."),(0,o.kt)("p",null,"This way, we can easily create packets that says 'hey, do something with your own controlled player'"),(0,o.kt)("p",null,"Conversely, we could define a ",(0,o.kt)("orange",null,"reference")," to each player connected on the session.",(0,o.kt)("br",null),"\nFor engine '",(0,o.kt)("orange",null,"n"),"', we would have reference ",(0,o.kt)("inlineCode",{parentName:"p"},"@session/players/n"),", and then,\nwe bind our ",(0,o.kt)("inlineCode",{parentName:"p"},"ControlledPlayer")," object to the network object reference where '",(0,o.kt)("orange",null,"n"),"' is equals to our actual ",(0,o.kt)("orange",null,"engine's identifier"),".",(0,o.kt)("br",null),"\nThis way, when we receive or send a player object, we are sure that it will be the ",(0,o.kt)("orange",null,"correct type")," depending on which engine the object lands,\nand the ",(0,o.kt)("orange",null,"conversion")," from the 'RemotePlayer' object of the ",(0,o.kt)("orange",null,"engine",(0,o.kt)("orange",null," that sends us our "),"own player instance")," (our ",(0,o.kt)("inlineCode",{parentName:"p"},"ControlledPlayer"),") will be ",(0,o.kt)("orange",null,"naturally performed"),"."),(0,o.kt)("h3",{id:"reinjecting-same-objects-references"},"Reinjecting same objects references"),(0,o.kt)("p",null,"Note: Do not be confused between ",(0,o.kt)("a",{href:a.Z.gnom.naming.nor},"Network Object Reference")," and a normal object reference. Object References, or a 'reference' is simply the normal reference of an object, the usual term to point to an instance (variables, fields etc) ",(0,o.kt)("a",{href:a.Z.gnom.naming.nor},"Network Object References are"),", as said multiple times, the reference of an object ",(0,o.kt)("orange",null,"threw the network"),"."),(0,o.kt)("p",null,"Using ",(0,o.kt)("orange",null,"normal objects"),", if you send twice the ",(0,o.kt)("orange",null,"same object")," to an engine, the engine will get ",(0,o.kt)("orange",null,"two different clones")," of the object you sent."),(0,o.kt)("p",null,"Using a ",(0,o.kt)("orange",null,"NetworkObject"),", you ensure that the object will have ",(0,o.kt)("orange",null,"only one instance")," of itself on engines, without having any",(0,o.kt)("orange",null,"undesirable")," clones."),(0,o.kt)("p",null,"Using ",(0,o.kt)("a",{href:i("fr.linkit.api.gnom.persistence.context.PersistenceConfig")},"PersistenceConfig "),"and ",(0,o.kt)("a",{href:"fr.linkit.api.gnom.reference.linker.ContextObjectLinker"},"ContextObjectLinker"),"]], it is possible to",(0,o.kt)("orange",null,"bind any object")," to a ",(0,o.kt)("orange",null,"network reference"),". ",(0,o.kt)("br",null),"Binding a regular object to a network reference using ContextObjectLinkers is sufficient for the objects to be handled as a ",(0,o.kt)("orange",null,"network object")," by the",(0,o.kt)("orange",null,"persistence system"),"."))}p.isMDXComponent=!0}}]);