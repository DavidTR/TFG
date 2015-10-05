#include "define.h"

extern TipoConfiguracion Conf;
extern TipoCentros Centros;
extern TTABLA tabla;

extern int n_datos,
           n_variables,
           k_inicial,
           t_inic,
           k_actual,
           *Seleccionados;
    
extern float semilla;

extern double *Ganancia;

extern unsigned long Seed;

extern char fich_datos[100];

/* Funciones */
extern double J (void), Distancia (double *, double *);
extern void Input (int, char**), Inicializa (void), Calcula_Centros (void);
extern int Cluster_Mas_Cercano (double *);

