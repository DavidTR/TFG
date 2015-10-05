#include "define.h"

TipoConfiguracion Conf;
TipoCentros Centros;
TTABLA tabla;

int n_datos,
    n_variables,
    k_inicial,
    t_inic,
    k_actual,
    *Seleccionados;
    
float semilla;

double *Ganancia;

unsigned long Seed;

char fich_datos[100];

/* Funciones */
double J (void), Distancia (double *, double *);
void Input (int, char**), Inicializa (void), Calcula_Centros (void);
int Cluster_Mas_Cercano (double *);

