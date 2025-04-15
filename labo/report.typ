#set page(
  header: align(right)[
    #set text(9pt)
    _#datetime.today().display()_
    #h(1fr)
    _DMA-L3 - Trilatération - Report_
  ],
  footer: [
    #set align(right)
    #set text(8pt)
    _Slimani, Steiner & Van Hove_
    #h(1fr)
    _#context(
      counter(page).display(
        "1 / 1",
        both: true,
      )
    )_
  ]
)

#set par(justify: true)

#align(center, text(20pt)[
  *DMA L3 Trilatération*
])
 
\
_Auteurs : Slimani, Steiner & Van Hove_
\
\

= 1 Lister les AP à portée
== 1.1
_Par rapport à un seul AP, que pouvez-vous dire sur la précision de la distance estimée ? Est-ce que la présence d'un obstacle (fenêtre, mur, personne) entre l'AP et le smartphone a une influence sur la précision ? Est-ce que faire une moyenne sur plusieurs mesures permet d'avoir une estimation plus fiable de la distance ?_

La précision de la distance estimée varie beaucoup en fonction des obstacles entre l'AP et le smartphone. En effet, la présence d'un obstacle comme une fenêtre, un mur ou une personne peut entraîner des erreurs de mesure significatives.
Comme les variations observées pour un AP à une distance de 3m varie entre 3m et 9m, une moyenne ne me parrait pas la meilleures des options. en effet on aurait une moyenne de 6m, ce qui n'est pas incroyable, par contre si au lieu de cela (et si lesmartphone n'est pas en mouvement) on prends la plus petite des mesures. De cette manière on pourrait estimer la distance à 3m. En effet, la distance mesurée est toujours supérieure à la distance réelle, donc en prenant la plus petite des mesures on peut estimer la distance réelle.

= 2 Déterminer la position du smartphone

Pour de faciliter le switch entre les deux configurations de localisation (la salle B30 en 2D et l'étage B en 3D), nous avons rendu la vue du fragment dynamique en ajoutant un RadioGroup qui permet de sélectionner le plan à utiliser. Lors d'un changement de sélection, la carte affichée, les APs et le mode de trilatération (2D ou 3D) sont automatiquement mis à jour grâce à l'observation de la variable `mapConfig` dans le ViewModel.

== 2.1
_Nous avons également placé des AP à différents endroits de l'étage B. La carte et la position de ces huit AP sont fournies dans le code. Pour activer une localisation sur l'étage B, il suffit de modifier la configuration placée dans la `LiveData_mapConfig` dans le `WifiRttViewModel`. Que pouvons-nous dire de la position obtenue en se promenant dans les couloirs de l'étage ? Doit-on tenir compte de tous les AP pour calculer la position ?_

On peut en dire que la position obtenue est assez précise, mais pas parfaite. En effet, la position est souvent décalée par rapport à la position réelle. De plus, il y a des endroits où la position est complètement fausse. Il est préférable de prendre en compte au moins 4 AP pour calculer la position, car cela permet d'obtenir une estimation plutôt précise de la position réelle. Avec 3 cela devrait fonctionner, mais il y a de fortes chances que la position soit fausse. En effet, avec 3 AP on peut avoir plusieurs solutions possibles. De plus, il faut faire attention à la distance entre les AP et le smartphone. Si le smartphone est trop loin d'un AP, la position calculée sera moins précise, idem avec des obstacles, ceux-ci influencent grandement la précision. 

== 2.2
_Pouvons-nous déterminer la hauteur du mobile par trilatération ? Si oui qu'est-ce que cela implique ? La configuration pour l'étage B contient la hauteur des AP et vous permet donc de faire des tests._

Oui, il est possible de déterminer la hauteur (coordonnée Z) d'un mobile en utilisant la trilatération, à condition d'avoir suffisamment d'informations en trois dimensions.

Dans un espace 2D, trois APs suffisent pour estimer la position (X, Y) du mobile. Mais dès qu'on souhaite ajouter la dimension verticale (Z), il est nécessaire d'utiliser les coordonnées complètes  des points d'accès (X, Y, Z) et de disposer d'au moins quatre points d'accès placés à des hauteurs différentes pour permettre une estimation fiable.

Cela implique que chaque point d'accès doit être précisément localisé, y compris en hauteur. La qualité de l'estimation dépendra également de la disposition des APs dans l'espace : plus ils sont répartis sur des hauteurs variées et dans différentes directions, plus la trilatération en 3D sera précise.

Dans le cas de la configuration du bâtiment B, la hauteur des points d'accès est connue, ce qui rend techniquement possible l'estimation de la position 3D complète (X, Y, Z) du mobile. Il est donc envisageable de mettre en place une trilatération tridimensionnelle pour localiser un utilisateur non seulement en surface, mais aussi en hauteur.

