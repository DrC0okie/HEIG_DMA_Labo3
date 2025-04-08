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

== 2.1
_Nous avons également placé des AP à différents endroits de l'étage B. La carte et la position de ces huit AP sont fournies dans le code. Pour activer une localisation sur l'étage B, il suffit de modifier la configuration placée dans la `LiveData_mapConfig` dans le `WifiRttViewModel`. Que pouvons-nous dire de la position obtenue en se promenant dans les couloirs de l'étage ? Doit-on tenir compte de tous les AP pour calculer la position ?_




== 2.2
_Pouvons-nous déterminer la hauteur du mobile par trilatération ? Si oui qu'est-ce que cela implique ? La configuration pour l'étage B contient la hauteur des AP et vous permet donc de faire des tests._

