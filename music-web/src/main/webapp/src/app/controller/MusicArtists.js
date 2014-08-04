'use strict';

/**
 * Artists library controller.
 */
angular.module('music').controller('MusicArtists', function($scope, $stateParams, $state, Restangular, filterFilter) {
  // Initialize filtering
  $scope.loaded = false;
  $scope.filter = $stateParams.filter;
  $scope.artists = [];
  $scope.filteredArtists = [];
  $scope.allArtists = [];
  var index = 0;
  
  // Load all artists
  Restangular.all('artist').getList().then(function(data) {
    $scope.allArtists = data.artists;
    $scope.loaded = true;
    $scope.filteredArtists = filterFilter($scope.allArtists, { name: $scope.filter });
    $scope.loadMore(true);
  });

  // Load more artists
  $scope.loadMore = function(reset) {
    if (reset)  {
      $scope.artists = [];
      index = 0;
    }
    $scope.artists = $scope.artists.concat($scope.filteredArtists.slice(index, index + 40));
    index += 40;
  };

  // Debounced version
  $scope.loadMoreDebounced = _.debounce($scope.loadMore, 50);

  // Keep the filter in sync with the view state
  $scope.$watch('filter', function() {
    $scope.filteredArtists = filterFilter($scope.allArtists,  { name: $scope.filter });
    $scope.loadMoreDebounced(true);

    $state.go('main.music.artists', {
      filter: $scope.filter
    }, {
      location: 'replace',
      notify: false
    });
  });
});