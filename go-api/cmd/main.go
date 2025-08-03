package main

import (
	_ "go-api/docs"
	adapter "go-api/internal/adapter/jsondatabase"
	router "go-api/internal/api"
	"go-api/internal/api/handler"
	"go-api/internal/application/services"

	"github.com/gin-gonic/gin"
)

func main() {
	r := initializeServer()
	r.Run(":3000")
}

func initializeServer() *gin.Engine {
	customerRepo := adapter.NewJSONCustomerRepository("data/customers.json")
	productRepo := adapter.NewJSONProductRepository("data/products.json")

	customerService := services.NewCustomerService(customerRepo)
	productService := services.NewProductService(productRepo)

	customerHandler := handler.NewCustomerHandler(customerService)
	productHandler := handler.NewProductHandler(productService)

	return router.SetUpRouter(customerHandler, productHandler)
}
