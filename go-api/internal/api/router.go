package api

import (
	"go-api/internal/api/handler"

	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
)

func SetUpRouter(customerHandler *handler.CustomerHandler, productHandler *handler.ProductHandler) *gin.Engine {
	router := gin.Default()

	// Clients
	router.GET("/api/customers/:id", customerHandler.GetCustomer)

	// Products
	router.GET("/api/products/:id", productHandler.GetProduct)

	// Docs
	router.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

	return router
}
